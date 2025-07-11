package com.wayos.command.langchain4j;

import com.wayos.Session;
import com.wayos.Hook.Match;

import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.command.CommandNode;

public class PromptCommandNode extends CommandNode {
		
    public PromptCommandNode(Session session, String [] hooks) {
    	
        super(session, hooks, Match.Head);
        		
    }

	@Override
	public String execute(MessageObject messageObject) {
		
		try {
			
			String sessionMemoryId = session.vars("#sessionId");
			
			String userMessage = cleanHooksFrom(messageObject.toString());
			
			System.out.println("Execute Prompt Command:");
			System.out.println("\tsessionMemoryId: " + sessionMemoryId);
			System.out.println("\tuserMessage: " + userMessage);
			
	    	Context context = session.context();
			
			context.load();
			
			String contextName = context.name();
			
	    	String url = context.prop("langchain4j.url");
	    	String modelName = context.prop("langchain4j.model");
	    	String systemPrompt = context.prop("langchain4j.systemPrompt");
	    	String docsDir = context.prop("langchain4j.docsDir");
						
			Agent agent = Langchain4JWayOSUtil.instance().agent(contextName, url, modelName, systemPrompt, docsDir);
			 
			return agent.chat(sessionMemoryId, userMessage);	    	
	    	
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return e.getMessage();
		}
		
	}
	
	
}

