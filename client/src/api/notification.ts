import client from './client';

export interface NotificationResponse {
    id: number;
    title: string;
    content: string;
    createdAt: string;
    isRead: boolean;
}

export const notificationApi = {
    getAll: async () => {
        // Based on grep, endpoint is /v1/notification
        const response = await client.get<NotificationResponse[]>('/v1/notification');
        return response.data;
    }
};
