package com.wayos.command.langchain4j;

import java.util.List;
import java.util.function.Consumer;

import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.command.AsyncCommandNode;
import com.wayos.command.AsyncTask;
import com.wayos.command.AsyncTask.Finish;
import com.wayos.pusher.WebPusher;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;

public class AsyncPromptRunner extends AsyncTask.Runner {
	
	@Override
	public void run(MessageObject messageObject) {
				
		AsyncCommandNode asyncCommandNode = getAsyncCommandNode();
		
		try {
						
			if (!isActive()) return;
			
			String sessionMemoryId = asyncCommandNode.session.vars("#sessionId");
			
			String userMessage = asyncCommandNode.cleanHooksFrom(messageObject.toString());
			
			System.out.println("Execute AsyncPrompt Command:");
			System.out.println("\tsessionMemoryId: " + sessionMemoryId);
			System.out.println("\tuserMessage: " + userMessage);
			
	    	Context context = asyncCommandNode.session.context();
			
			context.load();
			
			String contextName = context.name();
			
	    	String url = context.prop("langchain4j.url");
	    	String modelName = context.prop("langchain4j.model");
	    	String systemPrompt = context.prop("langchain4j.systemPrompt");
	    	String docsDir = context.prop("langchain4j.docsDir");
	    	
			StreamingAgent streamingAgent = Langchain4JWayOSUtil.instance().streamingAgent(contextName, url, modelName, systemPrompt, docsDir);
			
			TokenStream tokenStream = streamingAgent.chat(sessionMemoryId, userMessage);
			
			Consumer<String> partialResponseHandler = new Consumer<String>() {
				
				private boolean isFirstToken = true;
				
				private boolean isPartialParams = false;

				@Override
				public void accept(String partialResponse) {
					
					if (!isActive()) {
						
						finish(Finish.INTERRUPTED);
						
						return;
					}					
					
					if (partialResponse.contains("#")) {
						
						isPartialParams = true;
						
					}
					
					if (isPartialParams) {
						
						Langchain4JWayOSUtil.instance().streamingString(sessionMemoryId, partialResponse);
						
					} else {
						
						System.out.print(partialResponse);						
						
						String [] tokens = contextName.split("/");
						
						String accountId = tokens[0];
						
						String botId = tokens[1];							
						
						if (isFirstToken) {
							
					    	WebPusher.send(accountId, botId, sessionMemoryId, partialResponse, "begin");
					    	
					    	isFirstToken = false;
					    	
						} else {
							
					    	WebPusher.send(accountId, botId, sessionMemoryId, partialResponse, "partial");
					    	
						}
												
					}
					
				}
				
			};
			
			tokenStream.onPartialResponse(partialResponseHandler)
			.onRetrieved((List<Content> contents) -> { 
				
				System.out.println("Reading.."); 
				
			})
			.onToolExecuted((ToolExecution toolExecution) -> {
				
				System.out.println(toolExecution);
				
			})
			.onCompleteResponse((ChatResponse response) -> {
				
				if (!isActive()) {
					
					finish(Finish.INTERRUPTED);
					
					return;
				}
				
				System.out.println();
				
				String paramsBuffer = Langchain4JWayOSUtil.instance().streamingString(sessionMemoryId, null);
				
				finish(Finish.SUCCESS.put("tags", paramsBuffer));
				
			})
			.onError((Throwable error) -> {
				
				error.printStackTrace();
								
				finish(Finish.ERROR);
			})
			.start();			
			 
		} catch (Exception e) {
			
			e.printStackTrace();
			
			finish(Finish.ERROR);
		}
		
		
	}

}

