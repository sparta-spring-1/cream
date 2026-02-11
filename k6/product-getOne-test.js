import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        load_test: {
            executor: 'ramping-vus',
            stages: [
                { duration: '1m', target: 10 },   // 300 → 10
                { duration: '2m', target: 20 },   // 300 → 20
                { duration: '1m', target: 10 },
            ],
        },
    },
};
const BASE_URL = 'http://host.docker.internal:8080';
const ADMIN_TOKEN = 'Bearer 실제_관리자_JWT';

export default function () {

    const productId = Math.floor(Math.random() * 25) + 1;

    let res = http.get(`${BASE_URL}/v1/admin/products/${productId}`);

    check(res, {
        '200 OK': (r) => r.status === 200,

        'response has product id': (r) => {
            if (r.status !== 200) return false;
            if (!r.body || r.body.length === 0) return false;

            try {
                const body = JSON.parse(r.body);
                return body.id !== undefined;
            } catch (e) {
                return false;
            }
        },
    });


    // 실패 원인 확인용
    if (res.status !== 200) {
        console.error()
    }

    sleep(1);
}
