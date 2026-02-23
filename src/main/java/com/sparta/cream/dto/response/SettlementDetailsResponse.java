package com.sparta.cream.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.SettlementStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 정산 상세 정보를 나타내는 DTO 클래스입니다.
 * <p>
 * id, feeAmount, settlementAmount, totalAmount, status, settledAt, productName, tradeId, paymentId를 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
@AllArgsConstructor
public class SettlementDetailsResponse {
    private final Long id;
    private final BigDecimal feeAmount;
    private final BigDecimal settlementAmount;
    private final BigDecimal totalAmount;
    private final SettlementStatus status;
    private final LocalDateTime settledAt;
    private final String productName;
    private final Long tradeId;
    private final Long paymentId;

    public static SettlementDetailsResponse from(Settlement settlement) {
        return new SettlementDetailsResponse(settlement.getId(),
                settlement.getFeeAmount(),
                settlement.getSettlementAmount(),
                settlement.getTotalAmount(),
                settlement.getStatus(),
                settlement.getSettledAt(),
                settlement.getPayment().getTrade().getSaleBidId().getProductOption().getProduct().getName(),
                settlement.getPayment().getTrade().getId(),
                settlement.getPayment().getId());
    }
}
