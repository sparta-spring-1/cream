import client from './client';

export interface PaymentConfig {
    storeId: string;
    channelKey: string;
}

export interface PrepareResponse {
    id: number;          // Internal DB ID (used for path variable)
    paymentId: string;   // Merchant UID
    amount: number;
    productName: string;
    email: string;
    customerName: string;
    customerPhoneNumber: string;
}

export interface CompletePaymentRequest {
    impUid: string;
    merchantUid: string;
}

export interface CompletePaymentResponse {
    paymentId: number;
    status: string;
}

export const paymentApi = {
    // Get Store ID and Channel Key
    getConfig: async (): Promise<PaymentConfig> => {
        const response = await client.get<PaymentConfig>('/v1/payments/config');
        return response.data;
    },

    // Prepare Payment (Get Merchant UID)
    prepare: async (tradeId: number): Promise<PrepareResponse> => {
        const response = await client.post<PrepareResponse>('/v1/payments/prepare', { tradeId });
        return response.data;
    },

    // Complete Payment (Verify on Server)
    complete: async (paymentId: number, data: CompletePaymentRequest): Promise<CompletePaymentResponse> => {
        const response = await client.post<CompletePaymentResponse>(`/v1/payments/${paymentId}/complete`, data);
        return response.data;
    },

    getAllPayment: async () => {
        const response = await client.get<any>('/v1/payments');
        return response.data.content;
    },

    getDetails: async (paymentId: number) => {
        const response = await client.get(`/v1/payments/${paymentId}`);
        return response.data;
    },

    // Refund
    refund: async (paymentId: number, data: RefundRequest) => {
        const response = await client.post(`/v1/payments/${paymentId}/refund`, data);
        return response.data;
    }
};

export interface RefundRequest {
    tradeId: number;
    reason: string;
    amount: number;
}
