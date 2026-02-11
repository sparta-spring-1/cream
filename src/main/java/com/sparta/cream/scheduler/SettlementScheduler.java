package com.sparta.cream.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.domain.status.SettlementStatus;
import com.sparta.cream.repository.SettlementRepository;
import com.sparta.cream.service.PaymentService;
import com.sparta.cream.service.SettlementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "SettlementScheduler")
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final PaymentService paymentService;
    private final SettlementRepository settlementRepository;
    private final SettlementService settlementService;

    @Scheduled(cron = "0 0 23 * * *")
    public void createPendingSettlements() {
        log.info("[SettlementScheduler] 오후 11시 정산 설정 스케줄러 실행");
        List<Payment> paidPayments = paymentService.getByStatus(PaymentStatus.PAID_SUCCESS);
        settlementService.set(paidPayments);
        log.info("[SettlementScheduler] 오후 11시 정산 설정 스케줄러 완료");
    }

    @Scheduled(cron = "0 0 13 * * *")
    public void completePendingSettlements() {
        log.info("[SettlementScheduler] 오후 1시 정산 처리 스케줄러 실행");
        List<Settlement> pendingSettlements = settlementRepository.findByStatus(SettlementStatus.PENDING);
        settlementService.settle(pendingSettlements);
        log.info("[SettlementScheduler] 오후 1시 정산 처리 스케줄러 완료");
    }
}
