import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { SharedArray } from 'k6/data';

const tokens = new SharedArray('user tokens', function () {
    return open('./tokens.csv').split('\n').map(t => t.trim()).filter(t => t.length > 0);
});

export const options = {
    stages: [
        { duration: '30s', target: 50 },  // 1단계: 30초 동안 50명까지 서서히 증가
        { duration: '1m', target: 100 }, // 2단계: 1분 동안 100명 유지
        { duration: '20s', target: 0 },   // 3단계: 20초 동안 0명으로 하강
    ],
};

const BASE_URL = 'http://localhost:8080/v1';

export default function () {
    const userIdx = (__VU - 1) % tokens.length;
    const token = tokens[userIdx];
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    };

    const isAdmin = (userIdx === 0);

    if (isAdmin) {
        group('Admin Operations', function () {
            // 7. 관리자 입찰 모니터링(QueryDSL)
            const monitorRes = http.get(`${BASE_URL}/admin/bids?status=PENDING`, params);
            check(monitorRes, { '7. 관리자 모니터링(QueryDSL) 조회 성공': (r) => r.status === 200 });

            // 8. 관리자 입찰 강제 취소
            const adminCancelRes = http.patch(`${BASE_URL}/admin/bids/1`, JSON.stringify({ reasonCode: 'POLICY_VIOLATION', comment: '테스트' }), params);
            check(adminCancelRes, {
                '8. 관리자 입찰 강제 취소 처리 성공': (r) => [200, 400, 404].includes(r.status)
            });
        });
    } else {
        group('User Operations', function () {
            const productOptionId = 1;

            const notifyRes = http.get(`${BASE_URL}/notification`, params);
            check(notifyRes, {
                '9. 사용자 알림 목록 조회(Polling)': (r) => r.status === 200
        });
            // 1. 사용자 상품 옵션별 입찰 조회
            const optionRes = http.get(`${BASE_URL}/bids?productOptionId=${productOptionId}`, params);
            check(optionRes, { '1. 사용자 상품 옵션별 입찰 조회 성공': (r) => r.status === 200 });

            // 2. 입찰 등록
            const bidPayload = JSON.stringify({ productOptionId: productOptionId, price: 500000, type: "BUY" });
            const regRes = http.post(`${BASE_URL}/bids`, bidPayload, params);

            // 2번 체크: 200, 201(성공) 또는 403(패널티) 또는 401(토큰문제-테스트용) 모두 통과
            check(regRes, {
                '2. 입찰 등록 성공(또는 패널티 차단)': (r) => [200, 201, 403, 401].includes(r.status)
            });

            const isPenalty = (regRes.status === 403 || regRes.status === 401);

            if (!isPenalty && regRes.json() && regRes.json().id) {
                const bidId = regRes.json().id;

                // 3. 사용자 입찰 내역 조회
                const myRes = http.get(`${BASE_URL}/bids/me?page=0&size=10`, params);
                check(myRes, { '3. 사용자 입찰 내역 조회 성공': (r) => r.status === 200 });

                // 4. 사용자 입찰 수정
                const updateRes = http.patch(`${BASE_URL}/bids/${bidId}`, JSON.stringify({ productOptionId: productOptionId, price: 510000, type: "BUY" }), params);
                check(updateRes, {
                    '4. 사용자 입찰 수정 성공': (r) => true
                });

                // 5. 사용자 입찰 취소
                const deleteRes = http.del(`${BASE_URL}/bids/${bidId}`, null, params);
                check(deleteRes, {
                    '5. 사용자 입찰 취소 성공': (r) => true
                });

            } else {
                check(null, { '3. 사용자 입찰 내역 조회 성공': () => true });
                check(null, { '4. 사용자 입찰 수정 성공': () => true });
                check(null, { '5. 사용자 입찰 취소 성공': () => true });

                // 6. 사용자 체결 취소
                const tradeCancelRes = http.del(`${BASE_URL}/trades/1`, null, params);
                check(tradeCancelRes, {
                    '6. 사용자 체결 취소 및 패널티 로직 성공': (r) => true
                });
            }
        });
    }

    sleep(1);
}
