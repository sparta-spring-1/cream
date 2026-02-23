package com.sparta.cream.service;

import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.sparta.cream.dto.product.ProductImageUploadResponse;
import com.sparta.cream.dto.product.S3UploadResult;
import com.sparta.cream.entity.ProductImage;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ImageErrorCode;
import com.sparta.cream.repository.ProductImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 이미지 업로드와 관련된 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * 관리자에 의해 업로드된 이미지 파일을 검증한 후 AWS S3에 저장하고,
 * 저장된 이미지의 public 접근 URL을 반환합니다.
 *
 * @author heoarim
 * @since 2026. 2. 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

	private final S3Client s3Client;
	@Value("${aws.s3.bucket-name}")
	private String bucketName;
	private final ProductImageRepository productImageRepository;

	// 외부에서 사용, S3에 저장된 이미지 객체의 public url을 반환
	@Transactional
	public List<ProductImageUploadResponse> upload(List<MultipartFile> files) {

		List<S3UploadResult> s3UploadResults = files.stream()
			.map(this::uploadImage)
			.toList();

		List<ProductImage> newImages = new ArrayList<>();

		for (S3UploadResult s3UploadResult : s3UploadResults) {

			ProductImage newImage = new ProductImage(
				s3UploadResult.getOriginalFileName(),
				s3UploadResult.getObjectKey(),
				s3UploadResult.getUrl()
			);

			newImages.add(newImage);
		}

		// 각 파일을 업로드하고 url을 리스트로 반환
		return newImages.stream()
			.map(ProductImageUploadResponse::from)
			.toList();
	}

	// validateFile메서드를 호출하여 유효성 검증 후 uploadImageToS3메서드에 데이터를 반환하여 S3에 파일 업로드, public url을 받아 서비스 로직에 반환
	private S3UploadResult uploadImage(MultipartFile file) {
		validateFile(file.getOriginalFilename()); // 파일 유효성 검증
		return uploadImageToS3(file); // 이미지를 S3에 업로드하고, 저장된 파일의 public url을 서비스 로직에 반환
	}

	// 파일 유효성 검증
	private void validateFile(String filename) {
		// 파일 존재 유무 검증
		if (filename == null || filename.isEmpty()) {
			throw new BusinessException(ImageErrorCode.NOT_EXIST_FILE);
		}

		// 확장자 존재 유무 검증
		int lastDotIndex = filename.lastIndexOf(".");
		if (lastDotIndex == -1) {
			throw new BusinessException(ImageErrorCode.NOT_EXIST_FILE_EXTENSION);
		}

		// 허용되지 않는 확장자 검증
		String extension = URLConnection.guessContentTypeFromName(filename);
		List<String> allowedExtentionList = Arrays.asList("image/jpg", "image/jpeg", "image/png", "image/gif");
		if (extension == null || !allowedExtentionList.contains(extension)) {
			throw new BusinessException(ImageErrorCode.INVALID_FILE_EXTENSION);
		}
	}

	// 직접적으로 S3에 업로드
	private S3UploadResult uploadImageToS3(MultipartFile file) {
		// 원본 파일 명
		String originalFilename = file.getOriginalFilename();
		// 확장자 명
		String extension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf(".") + 1);
		// 변경된 파일
		String s3FileName = UUID.randomUUID().toString().substring(0, 10) + "_" + originalFilename;

		// 이미지 파일 -> InputStream 변환
		try (InputStream inputStream = file.getInputStream()) {
			// PutObjectRequest 객체 생성
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName) // 버킷 이름
				.key(s3FileName) // 저장할 파일 이름
				.acl(ObjectCannedACL.PUBLIC_READ) // 퍼블릭 읽기 권한
				.contentType("image/" + extension) // 이미지 MIME 타입
				.contentLength(file.getSize()) // 파일 크기
				.build();
			// S3에 이미지 업로드
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
		} catch (Exception exception) {
			throw new BusinessException(ImageErrorCode.IO_EXCEPTION_UPLOAD_FILE);
		}

		// public url 반환
		String ImageUrl = s3Client.utilities().getUrl(url -> url.bucket(bucketName).key(s3FileName)).toString();

		return new S3UploadResult(originalFilename, s3FileName, ImageUrl);
	}

	public void deleteImage(Long imageId) {
		ProductImage image = productImageRepository.findById(imageId)
			.orElseThrow(() -> new BusinessException(ImageErrorCode.NOT_EXIST_FILE));

		image.softDelete();
	}

	@Scheduled(cron = "0 0 0 * * *") // 매일 새벽 12시 실행
	@SchedulerLock(
		name = "product_image_cleanup_lock", // DB에 저장될 고유 키
		lockAtLeastFor = "1m",              // 최소 1분간은 다른 서버가 못 잡게 함
		lockAtMostFor = "5m"                // 최대 5분 후엔 자동으로 락 해제
	)
	public void cleanupOrphanedImages() {

		log.info("파일 삭제 시작: ");
		List<ProductImage> deletedImages = productImageRepository.findOrphanedImages();

		for (ProductImage img : deletedImages) {
			try {
				s3deleteFile(img.getUrl());
				productImageRepository.delete(img);
			} catch (Exception e) {
				// S3 삭제 실패 시 로그를 남기고, 다음 배치 때 다시 시도
				log.error("파일 삭제 실패: {}",img.getId());
			}
		}
	}

	public void s3deleteFile(String objectKey) {
		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.build();

			s3Client.deleteObject(deleteObjectRequest);
		} catch (Exception e) {
			throw new BusinessException(ImageErrorCode.FAIL_DELETE_FILE);
		}
	}
}
