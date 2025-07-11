package com.wayos.command.langchain4j;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface TestAgent {

	String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}

public class GemmaMemoryUserMessageToDefinedStringExample {

	  static String MODEL_NAME = "gemma3"; // try other local ollama model names
	  	  	  
	  static String BASE_URL = "http://localhost:11434"; // local ollama base url
	  
	  static String prompt = "You are thai massage therapist, answer very shortly";
	  
	  public GemmaMemoryUserMessageToDefinedStringExample () {
		  
		  System.out.println("Connect Ollama..");
		  
		  OllamaChatModel model = OllamaChatModel.builder()
	              .baseUrl(BASE_URL)
	              .modelName(MODEL_NAME)
	              .build();
		  
		  TestAgent agent = AiServices.builder(TestAgent.class)
				    .chatModel(model)
				    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
				    .systemMessageProvider(chatMemoryId -> prompt)
				    .build();
		  
		  String result1 = agent.chat("abc", "Hello, my name is wayOS");
		  		  
		  System.out.println(result1);
		  
		  String result2 = agent.chat("abc", "What is my name?");
		  
		  System.out.println(result2);
		  
	  }

	  public static void main(String[] args) {
		  
		  new GemmaMemoryUserMessageToDefinedStringExample();
		  
	  }

}
