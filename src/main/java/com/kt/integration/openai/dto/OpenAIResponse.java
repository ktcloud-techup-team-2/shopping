package com.kt.integration.openai.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenAIResponse {
	public record VectorCreate(
		String id,
		String name
	) {}

	public record Upload(
		String id,
		String filename
	) {}

	public record Search(
		List<SearchData> data
	) {}

	public record SearchData(
		@JsonProperty("file_id") String fileId,
		Double score,
		List<Content> content
	) {}

	public record Content(
		String type,
		String text
	) {}
}