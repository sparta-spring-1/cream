export interface SignupRequest {
    email: string;
    password: string;
    name: string;
    phoneNumber: string;
}

export interface SignupResponse {
    id: number;
    email: string;
    name: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
}

export interface MeResponse {
    id: number;
    email: string;
    name: string;
    createdAt: string;
    updatedAt: string;
}
