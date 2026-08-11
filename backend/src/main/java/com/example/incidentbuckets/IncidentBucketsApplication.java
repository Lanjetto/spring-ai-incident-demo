package com.example.incidentbuckets;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class IncidentBucketsApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(IncidentBucketsApplication.class, args);
        var chatClient = context.getBean(ChatClient.Builder.class).build();
        var chatClient1 = context.getBean(ChatClient.Builder.class);
        var chatModel = context.getBean(ChatModel.class);

        Prompt prompt = new Prompt(new UserMessage("Отвечай 3 словами. Расскажи факт о мире"));
        List<Generation> generations = chatModel.call(prompt).getResults();
        String text = generations.get(0).getOutput().getText();
        System.out.println(text);

//        var content = chatClient.prompt()
//                .user("Привет")
//                .call()
//                .content();
//        System.out.println(content);


        new IncidentBucketsApplication().funnyChatClient(chatClient1)
                .prompt()
                .user("Отвечай 3 словами. Расскажи факт о мире")
                .call()
                .chatClientResponse()
                .chatResponse()
                .getResults()
                .forEach(System.out::println);


        var entity = new IncidentBucketsApplication().chatClient(chatClient1)
                .prompt()
                .call()
                .entity(Dummy.class);
        System.out.println(entity);

//        var prompt = new Prompt(new UserMessage("Расскажи шутку"));
//        chatModel.

    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
    @Bean
    ChatClient funnyChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("Начинай любое сообщение с шутки")
                .defaultOptions(ChatOptions.builder()
                        .maxTokens(30)
                        .temperature(0.)
                )
                .build();
    }

}
