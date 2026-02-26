import client from './client';

export interface CancelTradeResponse {
    message: string;
}

// 체결 내역 아이템 타입 정의 (필요 시)
export interface TradeItem {
    id: number;
    productName: string;
    size: string;
    price: number;
    status: string;
    matchedAt: string;
    role: 'BUYER' | 'SELLER';
}

// 페이징 처리된 응답 타입 정의
export interface MyTradeResponse {
    content: TradeItem[];
    totalPages: number;
    totalElements: number;
    // 필요한 다른 페이징 필드들...
}

export const tradeApi = {
    /**
     * 내 체결 내역 조회 (추가된 부분)
     */
    getMyTrades: async (page: number, size: number) => {
        // 백엔드 컨트롤러의 @GetMapping 경로에 맞춰주세요.
        // 보통 /v1/trades/my 또는 /v1/trades 형태입니다.
        const response = await client.get<MyTradeResponse>(`/v1/trades/me`, {
            params: { page, size }
        });
        return response.data;
    },

    /**
     * 거래 취소
     */
    cancelTrade: async (tradeId: number) => {
        // 기존 코드가 delete로 되어 있으므로 유지하되,
        // 백엔드에서 @PatchMapping 등을 사용한다면 수정이 필요할 수 있습니다.
        const response = await client.delete<CancelTradeResponse>(`/v1/trades/${tradeId}`);
        return response.data;
    }
};
