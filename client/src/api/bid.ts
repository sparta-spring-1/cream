import client from './client';

export interface BidRequest {
    productOptionId: number; // Assuming we select an option (size)
    price: number;
    type: 'BUY' | 'SELL';
}

export interface BidResponse {
    id: number;
    price: number;
    type: 'BUY' | 'SELL';
    status: string;
    tradeId?: number; // Backend Requirement: Needs to be added to BidResponseDto
    createdAt?: string;
    productId?: number;
    productOptionId?: number; // Backend Requirement: Needs to be added to BidResponseDto
}

export const bidApi = {
    // Create a Bid
    create: async (data: BidRequest): Promise<BidResponse> => {
        const response = await client.post<BidResponse>('/v1/bids', data);
        return response.data;
    },

    // Get My Bids
    getMyBids: async (page = 0, size = 10) => {
        const response = await client.get(`/v1/bids/me?page=${page}&size=${size}`);
        return response.data;
    },

    getBidsByProduct: async (productOptionId: number) => {
        const response = await client.get(`/v1/bids?productOptionId=${productOptionId}`);
        return response.data;
    },

    updateBid: async (bidId: number, data: any) => {
        const response = await client.patch(`/v1/bids/${bidId}`, data);
        return response.data;
    },

    cancelBid: async (bidId: number) => {
        const response = await client.delete(`/v1/bids/${bidId}`);
        return response.data;
    }
};
