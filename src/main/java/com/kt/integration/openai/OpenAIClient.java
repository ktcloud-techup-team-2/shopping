package com.kt.integration.openai;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.kt.integration.openai.dto.OpenAIRequest;
import com.kt.integration.openai.dto.OpenAIResponse;

@HttpExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
public interface OpenAIClient {

	@PostExchange("/vector_stores")
	OpenAIResponse.VectorCreate createVectorStore(
		@RequestBody OpenAIRequest.VectorCreate request
	);

	@PostExchange(value = "/files", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
	OpenAIResponse.Upload uploadFile(
		@RequestBody MultiValueMap<String, Object> request
	);

	@PostExchange("/vector_stores/{vector_store_id}/files")
	void attachFileToVectorStore(
		@PathVariable("vector_store_id") String vectorStoreId,
		@RequestBody OpenAIRequest.UploadFile request
	);

	@PostExchange("/vector_stores/{vector_store_id}/search")
	OpenAIResponse.Search search(
		@PathVariable("vector_store_id") String vectorStoreId,
		@RequestBody OpenAIRequest.Search request
	);
}