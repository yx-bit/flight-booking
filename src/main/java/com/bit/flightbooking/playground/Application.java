package com.bit.flightbooking.playground;

import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestClient;

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
	CommandLineRunner ingestTermOfServiceToVectorStore(EmbeddingModel embeddingModel, VectorStore vectorStore,
													   @Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs) {
		return args -> {
			Optional<Driver> nativeClient = vectorStore.getNativeClient();

			if (nativeClient.isPresent()) {
				Driver driver = nativeClient.get();
				//判断是否存在数据
				if (driver.session().executeRead(tx -> tx.run("MATCH (n) RETURN count(n)").single().get(0).asLong() > 0)) {
					logger.info("Vector store already populated");
					return;
				}else{
					logger.info("load Document: ");
					// Ingest the document into the vector store
					vectorStore.write(new TokenTextSplitter().transform(new TextReader(termsOfServiceDocs).read()));
				}
				// Use the native client for Neo4j-specific operations
			}
			vectorStore.similaritySearch("Cancelling Bookings").forEach(doc -> {
				logger.info("Similar Document: {}", doc.getFormattedContent());
			});
		};
	}

	@Bean
	public VectorStore vectorStore(Driver driver, EmbeddingModel embeddingModel) {
		return Neo4jVectorStore.builder(driver, embeddingModel)

				.batchingStrategy(new TokenCountBatchingStrategy())
				.build();
	}

	@Bean
	public ChatMemory chatMemory() {
		return MessageWindowChatMemory.builder().build();
	}

	@Bean
	@ConditionalOnMissingBean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}


}
