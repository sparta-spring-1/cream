package com.sparta.cream.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.dto.response.CreatePaymentResponse;
import com.sparta.cream.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 결제 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 결제 사전 준비(Prepare) 등
 * 결제 프로세스의 전반적인 제어를 담당합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 26.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final TradeService tradeService;
	private final UserService userService;

	/**
	 * 결제 요청을 사전 준비하고 결제 엔티티를 저장합니다.
	 * <p>
	 * 거래(Trade)와 구매자(User) 정보를 바탕으로 merchantUid를 생성하고,
	 * 결제 상태를 READY로 설정하여 DB에 기록합니다.
	 * </p>
	 *
	 * @param tradeId 	거래 식별자
	 * @param userId  	구매자 식별자
	 * @return 프론트엔드로 전달할 결제 준비 완료 정보(DTO)
	 */
	public CreatePaymentResponse prepare(Long tradeId, Long userId) {

		Users buyer = userService.findById(userId);

		Trade trade = tradeService.findById(tradeId);

		String merchantUid = "PAY-" + LocalDate.now() + "-" + trade.getId().toString();
		String productName = trade.getPurchaseBidId().getProductOption().getProduct().getName();

		Payment payment = new Payment(merchantUid, productName, trade.getFinalPrice(), PaymentStatus.READY);
		paymentRepository.save(payment);

		return new CreatePaymentResponse(payment.getMerchantUid(),
			payment.getStatus().toString(),
			productName,
			payment.getAmount(),
			buyer.getEmail(),
			buyer.getName(),
			buyer.getPhoneNumber());
	}
}

// 테스트를 위한 dummy Data
// Entity 취합 후 삭제(리팩토링) 예정
class Trade {
	public Long getId() {
		return 1L;
	}

	public Long getFinalPrice() {
		return 5000L;
	}

	public PurchaseBid getPurchaseBidId() {
		return new PurchaseBid();
	}
}

class PurchaseBid {
	public ProductOption getProductOption() {
		return new ProductOption();
	}
}

class ProductOption {
	public Product getProduct() {
		return new Product();
	}
}

class Product {
	public String getName() {
		return "더미 상품 A";
	}
}

class Users {
	String email;
	Long id;

	Users(String email, Long id) {
		this.email = email;
		this.id = id;
	}

	public Long getId() {
		return 1L;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return "테스터";
	}

	public String getPhoneNumber() {
		return "010-1234-5678";
	}
}

@Service
class TradeService {
	public Trade findById(Long id) {
		return new Trade();
	}
}

@Service
class UserService {
	public Users findById(Long id) {
		return new Users("tester@example.com", 1L);
	}
}
