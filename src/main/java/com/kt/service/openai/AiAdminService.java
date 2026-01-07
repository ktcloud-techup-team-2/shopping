package com.kt.service.openai;

import com.kt.integration.openai.OpenAIClient;
import com.kt.integration.openai.OpenAIProperties;
import com.kt.integration.openai.dto.OpenAIRequest;
import com.kt.integration.openai.dto.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAdminService {

	private final OpenAIClient openAIClient;
	private final OpenAIProperties openAIProperties;

	public String createVectorStore(String name) {
		var request = new OpenAIRequest.VectorCreate(name, "FAQ 데이터 저장소");

		var response = openAIClient.createVectorStore(request);

		log.info("벡터 스토어 생성 완료: ID={}, Name={}", response.id(), response.name());
		return response.id();
	}

	public void uploadFaqFile(MultipartFile file) {
		String vectorStoreId = openAIProperties.faqVectorStoreId();

		if (vectorStoreId == null || vectorStoreId.isBlank()) {
			throw new IllegalStateException("application.yml에 faq-vector-store-id가 설정되지 않았습니다.");
		}

		try {
			var map = new LinkedMultiValueMap<String, Object>();

			var fileResource = new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			};

			map.add("purpose", "assistants");
			map.add("file", fileResource);

			OpenAIResponse.Upload uploadResponse = openAIClient.uploadFile(map);
			String fileId = uploadResponse.id();
			log.info("파일 업로드 성공: ID={}", fileId);

			openAIClient.attachFileToVectorStore(
				vectorStoreId,
				new OpenAIRequest.UploadFile(fileId)
			);
			log.info("벡터 스토어({})에 파일({}) 연결 완료", vectorStoreId, fileId);

		} catch (IOException e) {
			throw new RuntimeException("파일 처리 중 오류 발생", e);
		}
	}
}