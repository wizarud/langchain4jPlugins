package com.wayos.command.langchain4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

interface Agent {

	String chat(@MemoryId String memoryId, @UserMessage String userMessage);
	
}

interface StreamingAgent {

	TokenStream chat(@MemoryId String memoryId, @UserMessage String userMessage);

}

public class Langchain4JWayOSUtil {
	
	private static final int MAX_MESSAGE = 10;
	
	private static Langchain4JWayOSUtil langchain4JWayOSUtil;
	
	private Map<String, Agent> agentMap;
	
	private Map<String, StreamingAgent> streamingAgentMap;
	
	private Map<String, StringBuilder> streamingStringMap;

	private Langchain4JWayOSUtil() {
		
        agentMap = new HashMap<>();
		
        streamingAgentMap = new HashMap<>();
        
        streamingStringMap = new HashMap<>();
	}
	
	public static Langchain4JWayOSUtil instance() {
		
		if (langchain4JWayOSUtil==null) {
			
			langchain4JWayOSUtil = new Langchain4JWayOSUtil();
			
		}
		
		return langchain4JWayOSUtil;
	}
	
	public Agent agent(String contextName, String url, String modelName, String systemPrompt, String docsDir) {
		
		try {
				    		
	    	String agentKey;
	    	
	    	if (systemPrompt!=null && docsDir!=null) {
	    		
				agentKey = contextName + "." + url + "." + modelName + "." + systemPrompt + "." + docsDir;
	    		
	    		
	    	} else if (systemPrompt!=null) {
	    		
				agentKey = contextName + "." + url + "." + modelName + "." + systemPrompt;
				
	    	} else {
	    		
				agentKey = contextName + "." + url + "." + modelName;
				
	    	}
			
			Agent agent = agentMap.get(agentKey);
			
			if (agent==null) {
								  
				System.out.println("\tCreate Only One Agent for: " + agentKey);
				
				OllamaChatModel model = OllamaChatModel.builder()
			              .baseUrl(url)
			              .modelName(modelName)
			              .build();		
				
		    	if (systemPrompt!=null && docsDir!=null) {
		    		
		    		List<Document> documents = FileSystemDocumentLoader.loadDocuments(docsDir);	  
		    		
		    		System.out.println("Loading documents for RAG: " + documents.size());
		    		
		    		InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
		  		  
		    		EmbeddingStoreIngestor.ingest(documents, embeddingStore);		    		
		    		
					agent = AiServices.builder(Agent.class)
							  .chatModel(model)
							  .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGE))
							  .systemMessageProvider(chatMemoryId -> systemPrompt)
							  .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))	
							  .build();		    		
		    		
		    	} else if (systemPrompt!=null) {
		    		
					agent = AiServices.builder(Agent.class)
							  .chatModel(model)
							  .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGE))
							  .systemMessageProvider(chatMemoryId -> systemPrompt)
							  .build();
					
		    	} else {
		    		
					agent = AiServices.builder(Agent.class)
							  .chatModel(model)
							  .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGE))
							  .build();
					
		    	}
				
				agentMap.put(agentKey, agent);
				
			}
			
			return agent;			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
			
		}
		
	}
	
	public StreamingAgent streamingAgent(String contextName, String url, String modelName, String systemPrompt, String docsDir) {
		
    	String streamingAgentKey;
    	
    	if (systemPrompt!=null && docsDir!=null) {
    		
    		streamingAgentKey = contextName + "." + url + "." + modelName + "." + systemPrompt + "." + docsDir;
    		
    		
    	} else if (systemPrompt!=null) {
    		
    		streamingAgentKey = contextName + "." + url + "." + modelName + "." + systemPrompt;
			
    	} else {
    		
    		streamingAgentKey = contextName + "." + url + "." + modelName;
			
    	}
    	
    	
		try {				    		
			
	    	StreamingAgent streamingAgent = streamingAgentMap.get(streamingAgentKey);
			
			if (streamingAgent==null) {
								  
				System.out.println("\tCreate Only One Streaming Agent for: " + streamingAgentKey);
				
				OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
						.baseUrl(url)
						.modelName(modelName)
						.timeout(Duration.ofMinutes(15))
						.build();
								
		    	if (systemPrompt!=null && docsDir!=null) {
		    		
		    		List<Document> documents = FileSystemDocumentLoader.loadDocuments(docsDir);	  
		    		
		    		System.out.println("Loading documents for RAG Streaming Model: " + documents.size());
		    		
		    		InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
		  		  
		    		EmbeddingStoreIngestor.ingest(documents, embeddingStore);		    		
		    		
		    		streamingAgent = AiServices.builder(StreamingAgent.class)
							  .streamingChatModel(model)
							  .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGE))
							  .systemMessageProvider(chatMemoryId -> systemPrompt)
							  .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))	
							  .build();		    		
		    		
		    	} else if (systemPrompt!=null) {
		    		
		    		streamingAgent = AiServices.builder(StreamingAgent.class)
							  .streamingChatModel(model)
							  .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGE))
							  .systemMessageProvider(chatMemoryId -> systemPrompt)
							  .build();
					
		    	} else {
		    		
		    		streamingAgent = AiServices.builder(StreamingAgent.class)
							  .streamingChatModel(model)
							  .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGE))
							  .build();
					
		    	}	
		    	
				streamingAgentMap.put(streamingAgentKey, streamingAgent);
				
			}
			
			return streamingAgent;			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
			
		}
		
	}		
	
	public String streamingString(String memoryId, String partialResponse) {
		
		StringBuilder streamingStringBuilder = streamingStringMap.get(memoryId);
		
		if (streamingStringBuilder==null) {
			
			streamingStringBuilder = new StringBuilder();
			
			streamingStringMap.put(memoryId, streamingStringBuilder);
			
		} else if (partialResponse==null) {
			
			streamingStringMap.remove(memoryId);
			
			return streamingStringBuilder.toString();
			
		} 
		
		if (partialResponse!=null) {
			
			streamingStringBuilder.append(partialResponse);
			
		}
		
		return streamingStringBuilder.toString();

	}
	

}
