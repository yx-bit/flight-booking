package com.bit.flightbooking.playground.graph;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.QuestionClassifierNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Component
public class ProductFeedBackClassfierWorkFlow {

    @Bean
    public StateGraph productClassfierWorkFlow(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("input", new ReplaceStrategy());
            state.registerKeyAndStrategy("classifier_output", new ReplaceStrategy());
            state.registerKeyAndStrategy("solution", new ReplaceStrategy());
            return state;
        };
        // 评价正负分类节点
        QuestionClassifierNode feedbackClassifier = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .categories(List.of("positive feedback", "negative feedback"))
                .classificationInstructions(
                        List.of("Try to understand the user's feeling when he/she is giving the feedback."))
                .build();
        // 负面评价具体问题分类节点
        QuestionClassifierNode specificQuestionClassifier = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .categories(List.of("after-sale service", "transportation", "product quality", "others"))
                .classificationInstructions(List.of(
                        "What kind of service or help the customer is trying to get from us? " +
                                "Classify the question based on your understanding."))
                .build();
        StateGraph graph = new StateGraph("Consumer Service Workflow Demo", stateFactory)
                .addNode("feedback_classifier", node_async(feedbackClassifier))
                .addNode("specific_question_classifier", node_async(specificQuestionClassifier))
                .addNode("recorder", node_async(new RecordingNode()))
                // 定义边（流程顺序）
                .addEdge(START, "feedback_classifier")  // 起始节点
                .addConditionalEdges("feedback_classifier",
                        edge_async(new FeedbackQuestionDispatcher()),
                        Map.of("positive", "recorder", "negative", "specific_question_classifier"))
                .addConditionalEdges("specific_question_classifier",
                        edge_async(new SpecificQuestionDispatcher()),
                        Map.of("after-sale", "recorder", "transportation", "recorder",
                                "quality", "recorder", "others", "recorder"))
                .addEdge("recorder", END);  // 结束节点
        GraphRepresentation graphRepresentation = graph.getGraph(GraphRepresentation.Type.PLANTUML,
                "workflow graph");

        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        return graph;
    }

    public static class FeedbackQuestionDispatcher implements EdgeAction {
        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FeedbackQuestionDispatcher.class);
        @Override
        public String apply(OverAllState state) throws Exception {

            String classifierOutput = (String) state.value("classifier_output").orElse("");
            logger.info("classifierOutput: {}", classifierOutput);

            if (classifierOutput.contains("positive")) {
                return "positive";
            }
            return "negative";
        }


}
    public static class SpecificQuestionDispatcher implements EdgeAction {
        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpecificQuestionDispatcher.class);
        @Override
        public String apply(OverAllState state) throws Exception {

            String classifierOutput = (String) state.value("classifier_output").orElse("");
            logger.info("classifierOutput: {}", classifierOutput);

            Map<String, String> classifierMap = new HashMap<>();
            classifierMap.put("after-sale", "after-sale");
            classifierMap.put("quality", "quality");
            classifierMap.put("transportation", "transportation");

            for (Map.Entry<String, String> entry : classifierMap.entrySet()) {
                if (classifierOutput.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }

            return "others";
        }
    }
}
