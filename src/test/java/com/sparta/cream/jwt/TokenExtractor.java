package com.sparta.cream.jwt;

import org.junit.jupiter.api.Test;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

class TokenExtractor {

	@Test
	void extractTokensManually() throws Exception {
		String secretKey = "mfhaltSecretKey1234567890vmfhaltSecretKey1234567890_ExtraCharsFor64BytesCheck";

		String issuer = "app";
		long accessExp = 900L;
		long refreshExp = 1209600L;

		JwtProperties properties = new JwtProperties(secretKey, accessExp, refreshExp, issuer);
		JwtTokenProvider provider = new JwtTokenProvider(properties);

		Files.createDirectories(Paths.get("k6"));

		try (PrintWriter writer = new PrintWriter(new FileWriter("k6/tokens.csv"))) {
			for (long i = 1; i <= 100; i++) {
				String token = provider.createAccessToken(i);
				writer.println(token);
			}
		}

		System.out.println("[성공] 진짜 키를 사용한 tokens.csv 생성 완료");
	}
}
