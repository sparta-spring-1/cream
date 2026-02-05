package com.sparta.cream.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.SettlementStatus;
import com.sparta.cream.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "SettlementService")
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    @Transactional
    public void set(List<Payment> payments) {
        log.info("[SettlementService] 정산 설정 시작 (대상 결제 건 수: {})", payments.size());
        if (payments.isEmpty()) {
            log.info("[SettlementService] 정산 설정 대상 Payment가 없습니다.");
            return;
        }

        List<Settlement> newSettlements = payments.stream()
            .map(payment -> new Settlement(
                payment.getAmount(),
                SettlementStatus.PENDING,
                payment
            ))
            .toList();

        settlementRepository.saveAll(newSettlements);
        log.info("[SettlementService] 정산 설정 완료 (생성된 정산 건 수: {})", newSettlements.size());
    }

    @Transactional
    public void settle(List<Settlement> settlements) {
        log.info("[SettlementService] 정산 처리 시작 (대상 정산 건 수: {})", settlements.size());
        if (settlements.isEmpty()) {
            log.info("[SettlementService] 정산 처리 대상 Settlement가 없습니다.");
            return;
        }

        settlements.forEach(settlement -> {
            if (settlement.getStatus() == SettlementStatus.PENDING) {
                settlement.complete();
            } else {
                log.warn("[SettlementService] PENDING 상태가 아닌 정산 건(ID: {})이 settle 메서드에 전달되었습니다. 현재 상태: {}",
                    settlement.getId(), settlement.getStatus());
            }
        });

        settlementRepository.saveAll(settlements);
        log.info("[SettlementService] 정산 처리 완료 (처리된 정산 건 수: {})", settlements.size());
    }
}
