package com.kt.controller.faq;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatClient chatClient;

	@PostMapping
	public Map<String, String> chat(@RequestBody Map<String, String> request) {
		String userMessage = request.get("message");

		String aiResponse = chatClient.prompt()
			.user(userMessage)
			.call()
			.content();

		return Map.of("response", aiResponse);
	}
}