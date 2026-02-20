import client from './client';

export interface CancelTradeResponse {
    message: string;
}

export const tradeApi = {
    cancelTrade: async (tradeId: number) => {
        const response = await client.delete<CancelTradeResponse>(`/v1/trades/${tradeId}`);
        return response.data;
    }
};
