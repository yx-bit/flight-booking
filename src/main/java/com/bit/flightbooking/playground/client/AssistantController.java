package com.bit.flightbooking.playground.client;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.bit.flightbooking.playground.services.CustomerSupportAssistant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;


@RequestMapping("/api/assistant")
@RestController
public class AssistantController {

	private final CustomerSupportAssistant agent;
	private final CompiledGraph compiledGraph;

	private final ChatModel chatModel;

	private final ToolCallbackProvider[] toolCallbackProvider;
	public AssistantController(CustomerSupportAssistant agent, StateGraph stateGraph, ChatModel chatModel, ToolCallbackProvider[] toolCallbackProvider) throws GraphStateException {
		this.agent = agent;
        this.compiledGraph = stateGraph.compile();
        this.chatModel = chatModel;
        this.toolCallbackProvider = toolCallbackProvider;
    }

	@RequestMapping(path="/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> chat(@RequestParam("chatId")String chatId,@RequestParam("userMessage") String userMessage) {
		return agent.chat(chatId, userMessage);
	}

	@GetMapping("/chat")
	public String simpleChat(@RequestParam(name = "query") String query) throws Exception {

		return compiledGraph.invoke(Map.of("input", query)).get().value("solution").get().toString();
	}
	@GetMapping("/chat/stream")
	public Flux<String> simpleChatStream(@RequestParam(name = "query") String query) throws Exception {
		return ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).defaultToolCallbacks(toolCallbackProvider).build().prompt().user(query).stream().content();
	}
}
