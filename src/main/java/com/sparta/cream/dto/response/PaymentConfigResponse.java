package com.sparta.cream.dto.response;

import lombok.Getter;

/**
 * 결제 환경 설정 정보를 담은 응답 DTO입니다.
 * <p>
 * 프론트엔드에서 PortOne V2 SDK를 초기화하고 결제창을 호출하는 데 필요한
 * 상점 식별 정보 및 결제 채널 정보를 전달합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 26.
 */
@Getter
public class PaymentConfigResponse {

	private final String storeId;
	private final String channelKey;

	/**
	 * PaymentConfigResponse 생성자.
	 *
	 * @param storeId    상점 고유 식별자
	 * @param channelKey 결제 채널 키
	 */
	public PaymentConfigResponse(String storeId, String channelKey) {
		this.storeId = storeId;
		this.channelKey = channelKey;
	}
}
