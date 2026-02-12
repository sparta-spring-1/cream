import client from './client';

export interface BidRequest {
    productOptionId: number; // Assuming we select an option (size)
    price: number;
    type: 'BUY' | 'SELL';
}

export interface BidResponse {
    id: number;
    price: number;
    status: string;
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
    }
};
