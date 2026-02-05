import http from 'k6/http';

export const options = {
    vus: 5,
    duration: '10s',
};

export default function () {
    http.get("http://host.docker.internal:8080/v1/auth/signup");
}
