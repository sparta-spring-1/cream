import client from './client';

// === Product (Re-using generic types or defining specific ones if needed) ===
// We can assume Product API is already partially handled in product.ts, 
// but for Admin specific actions like 'delete', we might add them here or extend product.ts.
// For now, let's keep Admin specific APIs here.

export interface AdminProductResponse {
    id: number;
    name: string;
    modelNumber: string;
    brandName: string;
    productStatus: string;
    retailPrice: number;
}

// === Bid ===
export interface AdminBidCancelRequest {
    reasonCode: string; // e.g., "ABUSIVE_BID", "REQUESTED_BY_USER"
    comment: string;
}

export interface AdminBidMonitoringItem {
    bidId: number;
    userId: number;
    userName: string;
    productId: number;
    productName: string;
    categoryId: number;
    categoryName: string;
    price: number;
    type: string; // "BUY", "SELL"
    status: string;
    createdAt: string;
}

export interface PagingInfo {
    currentPage: number;
    totalElements: number;
    hasNext: boolean;
}

export interface AdminBidPagingResponse {
    items: AdminBidMonitoringItem[];
    paging: PagingInfo;
}

// === Trade ===
export interface AdminTradeMonitoringItem {
    tradeId: number;
    productName: string;
    price: number;
    status: string;
    sellerName: string;
    buyerName: string;
    createdAt: string;
}

export interface AdminTradePagingResponse {
    items: AdminTradeMonitoringItem[];
    paging: PagingInfo;
}

export interface ProductImageUploadResponse {
    imageId: number;
    originalFileName: string;
    objectKey: string;
    url: string;
}

export const adminApi = {
    // --- Product ---
    // Note: GetAll is in productApi, but Delete is admin specific
    deleteProduct: async (productId: number) => {
        await client.delete(`/v1/admin/products/${productId}`);
    },

    createProduct: async (data: any) => {
        const response = await client.post('/v1/admin/products', data);
        return response.data;
    },

    updateProduct: async (productId: number, data: any) => {
        const response = await client.put(`/v1/admin/products/${productId}`, data);
        return response.data;
    },

    // --- Bid ---
    monitorBids: async (page = 0, params?: {
        productId?: number,
        categoryId?: number,
        status?: string,
        type?: string,
        userId?: number
    }) => {
        const queryParams = new URLSearchParams();
        queryParams.append('page', page.toString());
        if (params) {
            if (params.productId) queryParams.append('productId', params.productId.toString());
            if (params.categoryId) queryParams.append('categoryId', params.categoryId.toString());
            if (params.status) queryParams.append('status', params.status);
            if (params.type) queryParams.append('type', params.type);
            if (params.userId) queryParams.append('userId', params.userId.toString());
        }

        const response = await client.get<any>(`/v1/admin/bids?${queryParams.toString()}`);
        // The backend returns { status: 200, message: "...", data: { items: [], paging: {} } }
        // We need to extract 'data'
        return response.data.data as AdminBidPagingResponse;
    },

    cancelBid: async (bidId: number, request: AdminBidCancelRequest) => {
        const response = await client.patch(`/v1/admin/bids/${bidId}`, request);
        return response.data;
    },

    // --- Trade ---
    monitorTrades: async (page = 0, params?: {
        status?: string,
        userId?: number
    }) => {
        const queryParams = new URLSearchParams();
        queryParams.append('page', page.toString());
        if (params) {
            if (params.status) queryParams.append('status', params.status);
            if (params.userId) queryParams.append('userId', params.userId.toString());
        }

        const response = await client.get<any>(`/v1/admin/trades?${queryParams.toString()}`);
        return response.data.data as AdminTradePagingResponse;
    },

    // --- Image ---
    uploadImage: async (file: File) => {
        const formData = new FormData();
        formData.append('image', file);
        const response = await client.post<ProductImageUploadResponse[]>('/v1/admin/images/upload', formData);
        return response.data;
    }
};
