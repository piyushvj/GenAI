package com.piyush.geni.controller;

import com.piyush.geni.advisor.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;

    @Value("classpath:/promptTemplates/userPromptTemplate.st")
    Resource userPromptTemplate;

    public ChatController(ChatClient.Builder chatClientBuilder) {

        ChatOptions chatOptions = ChatOptions
                .builder()
                .maxTokens(10)
                .temperature(0.8)
                .build();

        this.chatClient = chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultAdvisors(new TokenUsageAuditAdvisor())
                .defaultSystem("""
                        you are an HR assistant. Your role is to help employees with the questions related to HR policies.
                        If a user asks for help with anything outside of these topics,
                        kindly inform them that you can only assist with queries related to HR policies.
                        """)
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {


        return chatClient
                .prompt()
                .system("""
                        You are an internal IT helpdesk assistant. your role is to assist employees with IT-related issues such as resetting passwords,
                        unlocking accounts, and answering questions related to IT policies. If a user requests help with anything outside of these responsibilities,
                        response politely and inform them that you are only able to assist with IT support tasks within you defined scope.
                        """)
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/email")
    public String emailResponse(@RequestParam("customerName") String customerName,
                                @RequestParam("customerMessage") String customerMessage) {
        return chatClient
                .prompt()
                .system("""
                        You are a professional customer service assistant which helps drafting email responses to improve the productivity of the customer support team.
                        """)
                .user(promptUserSpec ->
                        promptUserSpec.text(userPromptTemplate)
                                .param("customerName", customerName)
                                .param("customerMessage", customerMessage))
                .call().content();

    }
}
