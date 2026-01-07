package com.kt.integration.openai;

import com.kt.integration.openai.dto.OpenAIRequest;
import com.kt.integration.openai.dto.OpenAIResponse;
import com.kt.integration.openai.dto.OpenAIResponse.SearchData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAICustomAdvisor implements CallAroundAdvisor {

	private final OpenAIClient openAIClient;
	private final OpenAIProperties openAIProperties;

	@Override
	public String getName() {
		return "OpenAICustomAdvisor";
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		String userMessage = advisedRequest.userText();
		String targetId = openAIProperties.faqVectorStoreId();

		if (targetId == null || targetId.isBlank()) {
			return chain.nextAroundCall(advisedRequest);
		}

		try {
			var response = openAIClient.search(
				targetId,
				new OpenAIRequest.Search(userMessage)
			);

			if (response.data() != null && !response.data().isEmpty()) {
				SearchData topData = response.data().stream()
					.max(Comparator.comparingDouble(SearchData::score))
					.orElse(null);

				if (topData != null && topData.content() != null) {
					String context = topData.content().stream()
						.map(OpenAIResponse.Content::text)
						.collect(Collectors.joining("\n"));

					String systemMessage = """
                        [Context]
                        %s
                        
                        위의 [Context]를 참고하여 답변해 주세요.
                        """.formatted(context);

					AdvisedRequest newRequest = AdvisedRequest.from(advisedRequest)
						.systemText(systemMessage)
						.build();

					return chain.nextAroundCall(newRequest);
				}
			}
		} catch (Exception e) {
			log.error("RAG 검색 실패 (무시하고 진행): {}", e.getMessage());
		}

		return chain.nextAroundCall(advisedRequest);
	}
}