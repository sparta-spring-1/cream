package com.sparta.cream.domain.notification.listener;

import com.sparta.cream.domain.event.PaymentCompletedEvent;
import com.sparta.cream.domain.bid.event.BidChangedEvent;
import com.sparta.cream.domain.notification.entity.NotificationType;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.event.TradeCancelledEvent;
import com.sparta.cream.domain.trade.event.TradeMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 시스템 내 도메인 이벤트를 구독하여 사용자 알림을 생성하는 이벤트 리스너입니다.
 * 입찰(Bid), 체결(Trade), 결제(Payment) 등 각 도메인에서 발행된 이벤트를 수집하고,
 * 비즈니스 로직에 맞는 알림 메시지를 생성하여 {@link NotificationService}로 전달합니다.
 * 주요 설계 원칙:
 * - 비동기 처리: @Async 를 사용하여 메인 트랜잭션의 성능에 영향을 주지 않고 알림을 처리합니다.
 * - 트랜잭션 격리: TransactionPhase.AFTER_COMMIT 설정을 통해 원본 비즈니스 로직이 최종 커밋된 경우에만 알림을 발송하여 데이터 무결성을 보장합니다.
 * - 느슨한 결합: 서비스 간 직접적인 의존성 대신 이벤트를 매개체로 사용하여 도메인 간 결합도를 낮추었습니다.
 * NotificationEventListener.Java
 *
 * @author kimsehyun
 * @since 2026. 2. 13.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationService notificationService;

	/**
	 * 입찰 관련 상태 변경 이벤트를 처리합니다.
	 * 입찰 등록, 수정, 취소등 이벤트 내에 포함된 타입과 메시지를 기반으로 알림을 생성합니다.
	 * @param event 입찰 변경 이벤트 데이터
	 */
	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleBidNotification(BidChangedEvent event) {
		send(event.userId(), event.type(), event.title(), event.message(), event.bidId());
	}

	/**
	 * 거래 체결 성공 이벤트를 처리합니다.
	 * 구매자와 판매자 모두에게 거래 완료 알림을 발송하며, 상품 정보 및 가격을 메시지에 포함합니다.
	 * @param event 거래 체결완료 이벤트 데이터
	 */
	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleTradeMatchedNotification(TradeMatchedEvent event) {
		String message = String.format("거래 체결! 옵션: %s, 금액: %,d원", event.productSize(), event.price());

		send(event.buyerId(), NotificationType.TRADE_MATCH, "거래 체결 완료", message, event.tradeId());
		send(event.sellerId(), NotificationType.TRADE_MATCH, "거래 체결 완료", message, event.tradeId());
	}

	/**
	 * 거래 취소 이벤트를 처리합니다.
	 * 취소당사자와 상대방 모두에게 알림을 발송하며,
	 * 취소유저는 패털티 알림을 상대방은 입찰 원복 메시지를 포합합니다.
	 * @param event 거래 취소 이벤트 데이터
	 */
	@Async("taskExecutor")
	@EventListener
	public void handleTradeCancelledNotification(TradeCancelledEvent event) {
		send(event.cancelUserId(),
			NotificationType.TRADE_CANCELLED,
			"체결 취소",
			"체결을 취소하여 3일간 입찰 등록이 제한됩니다.",
			event.tradeId());

		send(event.victimUserId(),
			NotificationType.TRADE_CANCELLED,
			"체결 취소",
			"상대방의 체결 취소로 입찰이 다시 대기 상태로 전환되었습니다.",
			event.tradeId());
	}

	/**
	 * 결제 완료 이벤트를 처리합니다.
	 * 결제가 최종 승인된 사용자에게 구매 확정 정보를 전달합니다.
	 * @param event 결제 와료 이벤트 데이터
	 */
	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentCompletedNotification(PaymentCompletedEvent event) {
		String message = String.format("[%s] 상품이 %,d원에 결제 완료되었습니다.", event.productName(), event.amount());

		send(event.userId(), NotificationType.PAYMENT_COMPLETE, "결제 완료", message, null);
	}

	/**
	 * 알림 생성을 위한 공통 내부 메서드 입니다.
	 * @param userId 알림 수신 대상 사용자 ID
	 * @param type 알림 유형
	 * @param title 알림 제목
	 * @param message 알림 본문 내용
	 * @param refId 관련 도메인 엔티티 ID
	 */
	private void send(Long userId, NotificationType type, String title, String message, Long refId) {
		notificationService.createNotification(userId, type, title, message, refId);


	}
}
