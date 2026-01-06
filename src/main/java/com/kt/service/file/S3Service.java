package com.kt.service.file;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Template s3Template;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${spring.cloud.aws.region.static}")
	private String region;

	@Value("${spring.servlet.multipart.max-file-size}")
	private DataSize maxFileSize;

	public List<String> uploadFiles(List<MultipartFile> files) {
		List<String> uploadedUrls = new ArrayList<>();

		if (files == null || files.isEmpty()) {
			return uploadedUrls;
		}

		for (MultipartFile file : files) {
			if (file.isEmpty()) continue;

			validateImageFile(file);

			String originalFileName = file.getOriginalFilename();
			String uuidFileName = UUID.randomUUID().toString() + ".webp";

			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();

				Thumbnails.of(file.getInputStream())
					.size(1000, 1000)
					.outputFormat("webp")
					.outputQuality(0.8)
					.toOutputStream(os);

				byte[] buffer = os.toByteArray();
				InputStream is = new ByteArrayInputStream(buffer);

				ObjectMetadata metadata = ObjectMetadata.builder()
					.contentType("image/webp")
					.build();

				s3Template.upload(bucket, uuidFileName, is, metadata);

				String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, uuidFileName);
				uploadedUrls.add(fileUrl);

			} catch (Exception e) {
				throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
			}
		}

		return uploadedUrls;
	}

	private void validateImageFile(MultipartFile file) {
		if (file.getSize() > maxFileSize.toBytes()) {
			throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image")) {
			throw new CustomException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
		}
	}
}