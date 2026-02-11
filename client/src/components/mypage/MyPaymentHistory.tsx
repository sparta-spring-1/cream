import { useEffect, useState } from 'react';
import { paymentApi } from '../../api/payment';

interface PaymentItem {
    id: number;
    merchantUid: string;
    productName: string;
    amount: number;
    status: string;
    paidAt: string;
}

const MyPaymentHistory = () => {
    const [payments, setPayments] = useState<PaymentItem[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        paymentApi.getAllPayment()
            .then(data => setPayments(data))
            .catch(err => console.error("Failed to fetch payments", err))
            .finally(() => setIsLoading(false));
    }, []);

    if (isLoading) return <div className="py-10 text-center text-gray-500">로딩 중...</div>;

    if (payments.length === 0) {
        return (
            <div className="py-20 flex flex-col items-center justify-center border-t border-b border-gray-100">
                <p className="text-gray-500">결제 내역이 없습니다.</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-4">
            {payments.map(payment => (
                <div key={payment.id} className="border border-gray-200 rounded-xl p-4 flex justify-between items-center">
                    <div className="flex flex-col gap-1">
                        <span className="font-bold text-sm">{payment.productName}</span>
                        <span className="text-xs text-gray-400">
                            {payment.paidAt && new Date(payment.paidAt).getFullYear() > 1970
                                ? new Date(payment.paidAt).toLocaleDateString()
                                : '-'}
                        </span>
                        <span className="text-xs text-gray-500">{payment.merchantUid}</span>
                    </div>
                    <div className="flex flex-col items-end gap-1">
                        <span className="font-bold">{payment.amount.toLocaleString()}원</span>
                        <span className={`text-xs font-bold ${payment.status === 'PAID' ? 'text-green-600' :
                            payment.status === 'CANCELLED' ? 'text-red-600' : 'text-gray-500'
                            }`}>
                            {payment.status === 'PAID' ? '결제완료' :
                                payment.status === 'CANCELLED' ? '취소됨' : payment.status}
                        </span>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default MyPaymentHistory;
