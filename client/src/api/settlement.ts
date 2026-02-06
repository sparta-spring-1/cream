import client from './client';

export interface SettlementListResponse {
    id: number;
    settlementAmount: number;
    status: 'PENDING' | 'COMPLETED';
    settledAt: string | null;
    productName: string;
}

export interface SettlementDetailsResponse {
    id: number;
    feeAmount: number;
    settlementAmount: number;
    totalAmount: number;
    status: 'PENDING' | 'COMPLETED';
    settledAt: string | null;
    productName: string;
    tradeId: number;
}

export const getSettlements = async () => {
    const response = await client.get<SettlementListResponse[]>('/v1/settlements');
    return response.data;
};

export const getSettlementDetails = async (settlementId: number) => {
    const response = await client.get<SettlementDetailsResponse>(`/v1/settlements/${settlementId}`);
    return response.data;
};
