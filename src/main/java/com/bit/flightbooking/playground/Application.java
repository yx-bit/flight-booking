package com.bit.flightbooking.playground;

import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;


@SpringBootApplication
public class Application  {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
	}

	// In the real world, ingesting documents would often happen separately, on a CI
	// server or similar.
	@Bean
	CommandLineRunner ingestTermOfServiceToVectorStore(EmbeddingModel embeddingModel,Driver driver, Neo4jEmbeddingStore vectorStore,
													   @Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs) {
		return args -> {
							//判断是否存在数据

				if (driver.session().executeRead(tx -> tx.run("MATCH (n) RETURN count(n)").single().get(0).asLong() > 0)) {
					logger.info("Vector store already populated");

				}else{
					logger.info("load Document: ");
					// Ingest the document into the vector store
//					vectorStore.write(new TokenTextSplitter().transform(new TextReader(termsOfServiceDocs).read()));
					TextDocumentParser parser = new TextDocumentParser();
					TextSegment textSegment = parser.parse(termsOfServiceDocs.getInputStream()).toTextSegment();
					textSegment.metadata().put("metadata.source",termsOfServiceDocs.getFilename());
					textSegment.metadata().put("metadata.charset","UTF-8");
					Embedding embed = embeddingModel.embed(textSegment).content();
					vectorStore.add(embed,textSegment);
				}
				// Use the native client for Neo4j-specific operations
			Embedding embed = embeddingModel.embed("Cancelling Bookings").content();

			EmbeddingSearchResult search = vectorStore.search(EmbeddingSearchRequest.builder().queryEmbedding(embed).build());
			List<EmbeddingMatch<TextSegment>> matches = search.matches();
			matches.forEach(match -> {
				logger.info("Similar Document: {}", match.embedded().text());

			});
		};
	}

//	@Bean
//	public EmbeddingStore<TextSegment> vectorStore(Driver driver) {
//
//		return Neo4jEmbeddingStore.builder()
//				.driver(driver)
//				.build();
//	}

	@Bean
	public ChatMemoryProvider chatMemory() {
		return chatId -> MessageWindowChatMemory.withMaxMessages(10);
	}
	@Bean
	public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
		return new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel);
	}

	@Bean
	@ConditionalOnMissingBean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}


}
