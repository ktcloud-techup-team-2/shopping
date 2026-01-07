package com.kt.integration.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpenAIProperties.class)
public class OpenAIConfiguration {

	private final OpenAIProperties openAIProperties;

	@Bean
	public OpenAIClient openAIClient(RestClient.Builder builder) {
		RestClient restClient = builder
			.baseUrl("https://api.openai.com/v1")
			.defaultHeader("Authorization", "Bearer " + openAIProperties.apiKey())
			.build();

		RestClientAdapter adapter = RestClientAdapter.create(restClient);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

		return factory.createClient(OpenAIClient.class);
	}

	@Bean
	public ChatClient chatClient(ChatClient.Builder builder, CallAroundAdvisor openAICustomAdvisor) {
		return builder
			.defaultAdvisors(openAICustomAdvisor)
			.build();
	}
}