package com.kt.controller.openai;

import com.kt.service.openai.AiAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
public class AdminFaqController {

	private final AiAdminService aiAdminService;

	@PostMapping("/init")
	public ResponseEntity<Map<String, String>> initVectorStore(@RequestParam String name) {
		String vectorStoreId = aiAdminService.createVectorStore(name);

		return ResponseEntity.ok(Map.of(
			"message", "벡터 스토어가 생성되었습니다. 아래 ID를 application.yml에 등록하세요.",
			"vectorStoreId", vectorStoreId
		));
	}

	@PostMapping("/upload")
	public ResponseEntity<String> uploadFaq(@RequestParam("file") MultipartFile file) {
		aiAdminService.uploadFaqFile(file);
		return ResponseEntity.ok("FAQ 파일이 성공적으로 업로드 및 처리되었습니다.");
	}
}