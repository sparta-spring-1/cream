import http from 'k6/http';

export const options = {
    vus: 5,
    duration: '10s',
};

export default function () {
    http.get("http://spring-app:8080/payment-test.html");
}
