package com.sparta.cream.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sparta.cream.dto.product.ProductImageUploadResponse;
import com.sparta.cream.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 전용 이미지 업로드 API를 제공하는 컨트롤러입니다.
 *
 * <p>
 * 상품 등록 이전 단계에서 이미지를 S3에 사전 업로드하기 위한 용도로 사용되며,
 * 업로드된 이미지는 특정 상품과 즉시 연결되지 않은 독립 리소스로 관리됩니다.
 * 해당 API는 관리자 권한을 가진 사용자만 접근 가능합니다.
 * </p>
 *
 * @author heoarim
 * @since 2026. 2. 2
 */
@RestController
@RequestMapping("/v1/admin/images")
@RequiredArgsConstructor
@Slf4j
public class AdminImageController {

	private final ImageService imageService;

	@PostMapping("/upload")
	public ResponseEntity<List<ProductImageUploadResponse>> s3Upload(@RequestPart(value = "image") List<MultipartFile> multipartFile) {
		log.info("파일 업로드 요청 개수: {}", multipartFile.size());
		List<ProductImageUploadResponse> upload = imageService.upload(multipartFile);
		return ResponseEntity.ok(upload);
	}

	@DeleteMapping("/{imageId}")
	public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
		imageService.deleteImage(imageId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
