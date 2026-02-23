package com.sparta.cream.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3UploadResult {
	private final String originalFileName;
	private final String objectKey;
	private final String url;
}
