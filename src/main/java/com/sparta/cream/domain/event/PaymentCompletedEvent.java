package com.sparta.cream.domain.event;

/**
 * 결제가 최종적으로 성공했을 때 발행되는 도메인 이벤트입니다.
 * 이 이벤트는 결제 로직(PaymentService)이 성공적으로 완료되었음을 알리며,
 * 이를 구독하는 리스너({@link com.sparta.cream.domain.notification.listener.NotificationEventListener})에 의해
 * 실시간 알림 전송 및 기타 후속 로직이 실행됩니다.
 * PaymentCompletedEvent.java
 *
 * @author kimsehyun
 * @since 2026. 2. 12.
 */
public record PaymentCompletedEvent(
	Long userId,
	String productName,
	Long amount,
	Long paymentId
) {}
