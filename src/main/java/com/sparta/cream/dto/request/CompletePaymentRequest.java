package com.sparta.cream.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 완료 요청 시 클라이언트로부터 받는 데이터를 나타내는 DTO 클래스입니다.
 * <p>
 * impUid(PortOne의 paymentId)와 merchantUid를 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
@NoArgsConstructor
public class CompletePaymentRequest {
    private String impUid;
    private String merchantUid;
}
