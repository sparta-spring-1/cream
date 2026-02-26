import client from './client';

export interface NotificationResponse {
    id: number;
    userId: number;
    tradeId: number | null;
    title: string;
    message: string;
    createdAt: string;
    readAt: string | null;
    isRead: boolean;
}

export interface NotificationPageResponse {
    items: NotificationResponse[];
    page: number;
    size: number;
    hasNext: boolean;
}

export const notificationApi = {
    getAll: async () => {
        const response = await client.get<NotificationPageResponse>('/v1/notification');
        return response.data.items;
    }
};
