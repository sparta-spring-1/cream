import { useEffect, useState } from 'react';
import { notificationApi, type NotificationResponse } from '../api/notification';

const NotificationPage = () => {
    const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        notificationApi.getAll()
            .then(data => setNotifications(data))
            .catch(err => console.error('알림 조회 실패:', err))
            .finally(() => setIsLoading(false));
    }, []);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        const controller = new AbortController();
        let reconnectTimeout: ReturnType<typeof setTimeout> | null = null;

        const connectSSE = async () => {
            try {
                const response = await fetch('/v1/notification/subscribe', {
                    method: 'GET',
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                    signal: controller.signal,
                });

                if (!response.ok) {
                    console.error('SSE 연결 실패:', response.status);
                    return;
                }

                if (!response.body) return;

                const reader = response.body.getReader();
                const decoder = new TextDecoder('utf-8');
                let buffer = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    buffer += decoder.decode(value, { stream: true });

                    const parts = buffer.split('\n\n');
                    buffer = parts.pop() || '';

                    for (const part of parts) {
                        if (part.startsWith('data:')) {
                            const json = part.replace(/^data:\s*/, '');

                            try {
                                const newNotif: NotificationResponse = JSON.parse(json);

                                setNotifications(prev => {
                                    const exists = prev.some(n => n.id === newNotif.id);
                                    if (exists) return prev;
                                    return [newNotif, ...prev];
                                });

                            } catch (err) {
                                console.error('알림 파싱 실패:', err);
                            }
                        }
                    }
                }
                
                reconnectTimeout = setTimeout(connectSSE, 3000);

            } catch (err) {
                if (!controller.signal.aborted) {
                    console.error('SSE 연결 오류:', err);
                    reconnectTimeout = setTimeout(connectSSE, 3000);
                }
            }
        };

        connectSSE();

        return () => {
            controller.abort();
            if (reconnectTimeout) clearTimeout(reconnectTimeout);
        };
    }, []);

    if (isLoading) {
        return <div className="p-10 text-center">Loading...</div>;
    }

    return (
        <div className="max-w-[800px] mx-auto py-10 px-4">
            <h1 className="text-2xl font-bold mb-6">알림 센터</h1>

            {notifications.length === 0 ? (
                <div className="text-gray-500 text-center py-20 bg-gray-50 rounded-xl">
                    새로운 알림이 없습니다.
                </div>
            ) : (
                <div className="flex flex-col gap-4">
                    {notifications.map(notif => (
                        <div
                            key={notif.id}
                            className={`p-4 rounded-xl border ${
                                notif.isRead
                                    ? 'bg-gray-50 border-gray-100'
                                    : 'bg-white border-black/10 shadow-sm'
                            }`}
                        >
                            <div className="flex justify-between items-start mb-2">
                                <h3 className="font-bold">{notif.title}</h3>
                                <span className="text-xs text-gray-400">
                                    {new Date(notif.createdAt).toLocaleDateString()}
                                </span>
                            </div>
                            <p className="text-sm text-gray-600">{notif.message}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default NotificationPage;
