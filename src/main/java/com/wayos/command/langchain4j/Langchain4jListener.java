package com.wayos.command.langchain4j;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import com.wayos.Session;
import com.wayos.command.AsyncCommandNode;
import com.wayos.command.wakeup.ExtensionSupportWakeupCommandNode;

@WebListener
public class Langchain4jListener extends ExtensionSupportWakeupCommandNode.WebListener {

	@Override
	public void wakup(Session session) {
		
		session.commandList().add(new PromptCommandNode(session, new String[]{"promptCMD"}));
		session.commandList().add(new AsyncCommandNode(session, new String[]{"spromptCMD"}, new AsyncPromptRunner()));		
		
		System.out.println(session + " prompt & sprompt command ready..");
		
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		super.contextInitialized(sce);
		
		/**
		 * TODO: Check why too many load!!!!
		 */
		
		Map<String, Map<String, String>> logicDesignerExtToolMap = 
				(Map<String, Map<String, String>>)sce.getServletContext().getAttribute("logicDesignerExtToolMap");		
		
		Map<String, String> sampleEntity1Map = new HashMap<>();
		sampleEntity1Map.put("tool-label", "llm");
		sampleEntity1Map.put("tool-color", "#E6CFA9");
		sampleEntity1Map.put("tool-tip", "LLM Prompt, You have to config the following langchain4j.url and langchain4j.model including the optional langchain4j.systemPrompt or langchain4j.docsDir (RAG) properties!");
		sampleEntity1Map.put("entity-resps", "["
				+ "{"
				+ "	txt: 'CMD',"
				+ "	params: [{ parameterName: 'hook', value: 'promptCMD' }, { parameterName: 'params', value: 'Tell me what you want to do' }]"
				+ "}"
				+ "]");
		
		Map<String, String> sampleEntity2Map = new HashMap<>();
		sampleEntity2Map.put("tool-label", "sllm");
		sampleEntity2Map.put("tool-color", "#C1856D");
		sampleEntity2Map.put("tool-tip", "Streaming LLM Prompt, You have to config the following langchain4j.url and langchain4j.model including the optional langchain4j.systemPrompt or langchain4j.docsDir (RAG) properties!");
		sampleEntity2Map.put("entity-resps", "["
				+ "{"
				+ "	txt: 'CMD',"
				+ "	params: [{ parameterName: 'hook', value: 'spromptCMD' }, { parameterName: 'params', value: 'Tell me what you want to do' }]"
				+ "}"
				+ "]");
		
		/**
		 * DOM Id query pattern to apply colour
		 * extCommand-<Hook>
		 */
		logicDesignerExtToolMap.put("extCommand-promptCMD", sampleEntity1Map);
		logicDesignerExtToolMap.put("extCommand-spromptCMD", sampleEntity2Map);
		
		System.out.println("Loaded Sample Tools: " + sce.getServletContext().getAttribute("logicDesignerExtToolMap"));		
		
	}	

}
