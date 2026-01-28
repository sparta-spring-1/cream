package com.sparta.cream.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	String secret,
	long accessExpSec,
	long refreshExpSec,
	String issuer
) {}

