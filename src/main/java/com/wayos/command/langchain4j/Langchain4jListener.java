package com.wayos.command.langchain4j;

import javax.servlet.annotation.WebListener;

import com.wayos.Session;
import com.wayos.command.AsyncCommandNode;
import com.wayos.command.wakeup.ExtensionSupportWakeupCommandNode;

@WebListener
public class Langchain4jListener extends ExtensionSupportWakeupCommandNode.WebListener {

	@Override
	public void wakup(Session session) {
		
		session.commandList().add(new PromptCommandNode(session, new String[]{"prompt"}));
		session.commandList().add(new AsyncCommandNode(session, new String[]{"aprompt"}, new AsyncPromptRunner()));		
		
		System.out.println(session + " prompt & aprompt command ready..");
		
	}

}
