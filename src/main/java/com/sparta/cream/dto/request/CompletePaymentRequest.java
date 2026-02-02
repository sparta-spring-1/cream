package com.sparta.cream.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompletePaymentRequest {
    private String impUid;
    private String merchantUid;
}
