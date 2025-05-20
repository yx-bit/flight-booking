/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bit.flightbooking.playground.services;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;


/**
 * * @author Christian Tzolov
 */
@Service
public class CustomerSupportAssistant {

    private final BookingAssistant chatClient;

	public CustomerSupportAssistant(BookingAssistant chatClient) {
		this.chatClient = chatClient;
	}

//    public CustomerSupportAssistant(OpenAiChatModel chatModel, StreamingChatModel streamingChatModel, ContentRetriever contentRetriever, List<ToolSpecification> toolSpecifications) {
//
//        // @formatter:off
//		this.chatClient = AiServices.builder(BookingAssistant.class)
//				.chatModel(chatModel)
//				.streamingChatModel(streamingChatModel)
//				.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
//				.contentRetriever(contentRetriever)
//				.tools(toolSpecifications)
//
//				.build();
//		// @formatter:on
//    }

    public Flux<String> chat(String chatId, String userMessageContent) {
//        return this.chatClient.prompt()
//                .system(s -> s.param("current_date", LocalDate.now().toString()))
//                .user(userMessageContent)
//                .advisors(a -> a.param(CONVERSATION_ID, chatId).param(TOP_K, 100))
//                .stream()
//                .content();

		return this.chatClient.chat(chatId, LocalDate.now().toString(),userMessageContent);
    }

}