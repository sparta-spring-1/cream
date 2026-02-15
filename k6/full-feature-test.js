import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { SharedArray } from 'k6/data';

const tokens = new SharedArray('user tokens', function () {
    return open('./tokens.csv').split('\n').map(t => t.trim()).filter(t => t.length > 0);
});

export const options = {
    stages: [
        { duration: '1m', target: 500 },   // 1단계: 1분 동안 500명까지 램프업 (완만하게 시작)
        { duration: '2m', target: 1000 },  // 2단계: 2분 동안 1000명 유지 (본격적인 부하)
        { duration: '1m', target: 1000 },  // 3단계: 1000명 상태로 1분 더 버티기 (내구성 확인)
        { duration: '30s', target: 0 },    // 4단계: 서서히 종료
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
        responseCallback: http.expectedStatuses({ min: 200, max: 499 }),
    };

    const isAdmin = (userIdx === 0);

    if (isAdmin) {
        group('Admin Operations', function () {
            // 7. 관리자 입찰 모니터링(QueryDSL)
            const monitorRes = http.get(`${BASE_URL}/admin/bids?status=PENDING`, params);
            check(monitorRes, { '7. 관리자 모니터링(QueryDSL) 조회 성공': (r) => r.status === 200 });

            // 8. 관리자 입찰 강제 취소
            if (Math.random() < 0.2) {
            const adminCancelRes = http.patch(`${BASE_URL}/admin/bids/1`, JSON.stringify({ reasonCode: 'POLICY_VIOLATION', comment: '테스트' }), params);
            check(adminCancelRes, {
                '8. 관리자 입찰 강제 취소 처리 성공': (r) => [200, 400, 404].includes(r.status)
            });
            }
        });
    } else {
        group('User Operations', function () {
            const productOptionId = 1;

            // 9. 사용자 알림 목록 조회(Polling)
            const notifyRes = http.get(`${BASE_URL}/notification`, params);
            check(notifyRes, {'9. 사용자 알림 목록 조회(Polling)': (r) => r.status === 200});

            // 1. 사용자 상품 옵션별 입찰 조회
            const optionRes = http.get(`${BASE_URL}/bids?productOptionId=${productOptionId}`, params);
            check(optionRes, { '1. 사용자 상품 옵션별 입찰 조회 성공': (r) => r.status === 200 });

            const type = (userIdx % 2 === 0) ? "BUY" : "SELL";
            const randomPrice = (Math.floor(Math.random() * 21) + 490) * 1000;

            // 2. 입찰 등록
            const bidPayload = JSON.stringify({ productOptionId, price: randomPrice, type });
            const regRes = http.post(`${BASE_URL}/bids`, bidPayload, params);

            check(regRes, {'2. 입찰 등록 성공(또는 패널티 차단)': (r) => {return [200, 201, 400, 403,408,429].includes(r.status);
                },
            });

            const isPenalty = (regRes.status === 403 || regRes.status === 401);

            if (!isPenalty && regRes.json() && regRes.json().id) {
                const bidId = regRes.json().id;


                // 3. 사용자 입찰 내역 조회
                const myRes = http.get(`${BASE_URL}/bids/me?page=0&size=10`, params);
                check(myRes, { '3. 사용자 입찰 내역 조회 성공': (r) => r.status === 200 });

                sleep(Math.random() * 5 + 5);

                const dice = Math.random();

                if (dice < 0.3) {

                    // 4. 사용자 입찰 수정
                    const updatePayload = JSON.stringify({
                        productOptionId: productOptionId,
                        price: randomPrice + 10000,
                        type: type
                    });
                    const updateRes = http.patch(`${BASE_URL}/bids/${bidId}`, updatePayload, params);
                    check(updateRes, {
                        '4. 사용자 입찰 수정 시도 완료(정상 응답)': (r) => {
                            const expectedStatuses = [200, 201, 204, 400, 401, 403, 408, 409];
                            return expectedStatuses.includes(r.status);
                        }
                    });
                    sleep(1);


                    // 5. 사용자 입찰 취소
                    const deleteRes = http.del(`${BASE_URL}/bids/${bidId}`, null, params);

                    const expectedCancelStatuses = [200, 204, 400, 401, 403, 404, 408, 409, 429];

                    check(deleteRes, {
                        '5. 사용자 입찰 취소 시도 완료': (r) => expectedCancelStatuses.includes(r.status)
                    });

                    if (!expectedCancelStatuses.includes(deleteRes.status)) {
                        console.warn(`서버 내부 오류 발생! 코드: ${deleteRes.status} | 바디: ${deleteRes.body}`);
                    }
                }
                else if (dice < 0.5) {

                // 6. 사용자 체결 취소
                const tradeCancelRes = http.del(`${BASE_URL}/trades/1`, null, params);

                if (![200, 400, 403, 404, 429, 500].includes(tradeCancelRes.status)) {
                    console.warn(`[DEBUG-6] 실패 유저: ${userIdx} | 상태코드: ${tradeCancelRes.status} | 응답: ${tradeCancelRes.body}`);
                }

                check(tradeCancelRes, {
                    '6. 사용자 체결 취소 및 패널티 로직 성공': (r) => [200,401, 400, 403, 404,429].includes(r.status)
                })
                }
            }
        });
    }

    sleep(0.5);
}
