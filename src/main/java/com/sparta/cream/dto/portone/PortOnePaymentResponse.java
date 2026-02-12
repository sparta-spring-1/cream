package com.sparta.cream.dto.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PortOne API에서 결제 정보를 조회할 때 반환되는 응답 데이터를 나타내는 DTO 클래스입니다.
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOnePaymentResponse {
    private Amount amount;
    private Method method;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Amount {
        private int total;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Method {
        private String type;
    }
}
