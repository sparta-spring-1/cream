import { useEffect, useState } from 'react';
import { notificationApi, NotificationResponse } from '../api/notification';

const NotificationPage = () => {
    const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        notificationApi.getAll()
            .then(data => setNotifications(data))
            .catch(err => console.error(err))
            .finally(() => setIsLoading(false));
    }, []);

    if (isLoading) return <div className="p-10 text-center">Loading...</div>;

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
                        <div key={notif.id} className={`p-4 rounded-xl border ${notif.isRead ? 'bg-gray-50 border-gray-100' : 'bg-white border-black/10 shadow-sm'}`}>
                            <div className="flex justify-between items-start mb-2">
                                <h3 className="font-bold">{notif.title}</h3>
                                <span className="text-xs text-gray-400">{new Date(notif.createdAt).toLocaleDateString()}</span>
                            </div>
                            <p className="text-sm text-gray-600">{notif.content}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default NotificationPage;
