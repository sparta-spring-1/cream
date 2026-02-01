package com.sparta.cream.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 접근을 위한 S3Client 설정 클래스입니다.
 *
 * <p>
 * 접근 키, 시크릿 키, 리전 정보는 외부 설정(application.yml 또는 환경 변수)을 통해 주입받으며,
 * 코드 레벨에서 AWS 인증 정보를 직접 노출하지 않도록 설계되었습니다.
 * </p>
 *
 *  @author heoarim
 *  @since 2026. 2. 2
 */
@Configuration
public class S3Config {

	@Value("${aws.s3.access-key}")
	private String accessKey;
	@Value("${aws.s3.secret-key}")
	private String secretKey;
	@Value("${aws.region}")
	private String region;

	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
		return S3Client.builder()
			.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
			.region(Region.of(region))
			.build();
	}

}
