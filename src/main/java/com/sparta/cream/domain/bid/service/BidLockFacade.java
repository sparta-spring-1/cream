package com.sparta.cream.domain.bid.service;

import com.sparta.cream.domain.bid.dto.BidCancelResponseDto;
import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 입찰 작업에 대한 분산 락을 관리하는 퍼사드 클래스입니다.
 * DB 트랜잭션이 시작되기 전 Redisson을 통해 락을 선점함으로써
 * 고동시성 환경에서 데이터 정합성을 유지하고 DB 커넥션 고갈을 방지합니다.
 * BidLockFacade.java
 *
 * @author kimsehyun
 * @since 2026. 02. 10.
 */
@Component
@RequiredArgsConstructor
public class BidLockFacade {

	private final RedissonClient redissonClient;
	private final BidService bidService;

	/**
	 * 상품 옵션별 분산 락을 획득하여 새로운 입찰을 등록합니다.
	 * 락 획득 후 실제 DB 작업은 {@link BidService#createBid}로 위임합니다.
	 *
	 * @param userId 입찰 등록 사용자 ID
	 * @param requestDto 입찰 요청 정보
	 * @return 등록된 입찰 정보
	 */
	public BidResponseDto createBidWithLock(Long userId, BidRequestDto requestDto) {
		RLock lock = redissonClient.getLock("lock:option:" + requestDto.getProductOptionId());
		try {
			if (!lock.tryLock(3,5, TimeUnit.SECONDS)) {
				throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
			}
			return bidService.createBid(userId, requestDto);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		} finally {
			if (lock.isHeldByCurrentThread()) lock.unlock();
		}
	}

	/**
	 * 상품 옵션별 분산 락을 획득하여 기존 입찰 정보를 수정합니다.
	 * 수정 중인 입찰이 동시에 취소 되거나 체결되는 것을 방지하기 위해
	 * 해당 입찰 고유 식별자로 락을 획득합니다.
	 *
	 * @param userId 수정 요청 사용자 ID
	 * @param bidId 수정할 입찰 ID
	 * @param requestDto 수정 정보
	 * @return 수정된 입찰 정보
	 */
	public BidResponseDto updateBidWithLock(Long userId, Long bidId, BidRequestDto requestDto) {
		RLock lock = redissonClient.getLock("lock:bid:update:" + bidId);
		try {
			if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
				return bidService.updateBid(userId, bidId, requestDto);
			} else {
				throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		} finally {
			if (lock.isHeldByCurrentThread()) lock.unlock();
		}
	}

	/**
	 * 특정 입찰 ID 기반으로 락을 획득하여 입찰을 취소합니다.
	 * 락 범위를 개별 입찰(Bid)로 좁혀 상품 옵션 전체에 대한 병목 현상을 최소화합니다.
	 *
	 * @param userId 취소 요청 사용자 ID
	 * @param bidId 취소할 입찰 ID
	 * @return 취소 결과 응답
	 */
	public BidCancelResponseDto cancelBidWithLock(Long userId, Long bidId) {
		RLock lock = redissonClient.getLock("lock:bid:cancel:" + bidId);

		try {
			if (!lock.tryLock(15, TimeUnit.SECONDS)) {
				throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
			}
			return bidService.cancelBid(userId, bidId);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		} finally {
			if (lock.isHeldByCurrentThread()) lock.unlock();
		}
	}

}
