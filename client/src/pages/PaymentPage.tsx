import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { paymentApi, type PrepareResponse } from '../api/payment';

const PaymentPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const tradeId = searchParams.get('tradeId');

    const [isLoading, setIsLoading] = useState(false);
    const [paymentData, setPaymentData] = useState<PrepareResponse | null>(null);

    useEffect(() => {
        // Initialize PortOne V2 SDK
        const portoneScript = document.createElement("script");
        portoneScript.src = "https://cdn.portone.io/v2/browser-sdk.js";
        document.head.appendChild(portoneScript);

        // Fetch Payment Data if tradeId exists
        if (tradeId) {
            paymentApi.prepare(Number(tradeId))
                .then(data => setPaymentData(data))
                .catch(err => {
                    console.error("Failed to prepare payment", err);
                    alert("결제 정보를 불러오는데 실패했습니다.");
                    navigate(-1);
                });
        }

        return () => {
            document.head.removeChild(portoneScript);
        }
    }, [tradeId, navigate]);

    const handlePayment = async () => {
        if (!paymentData || !tradeId) return;

        try {
            setIsLoading(true);

            // 1. Get Config (Store ID, Channel Key)
            const config = await paymentApi.getConfig();
            const { PortOne } = window as any;

            // 2. Request Payment via PortOne V2 SDK
            const response = await PortOne.requestPayment({
                storeId: config.storeId,
                channelKey: config.channelKey,
                paymentId: paymentData.paymentId,
                orderName: paymentData.productName,
                totalAmount: paymentData.amount,
                currency: 'CURRENCY_KRW',
                payMethod: 'CARD',
                customer: {
                    email: paymentData.email,
                    fullName: paymentData.customerName,
                    phoneNumber: paymentData.customerPhoneNumber,
                },
            });

            if (response?.code !== undefined) {
                alert(`결제에 실패하였습니다. 에러내용: ${response.message}`);
                return;
            }

            // 3. Complete Payment on Server
            await paymentApi.complete(paymentData.id, {
                impUid: response.txId,
                merchantUid: response.paymentId,
            });

            alert('결제가 성공적으로 완료되었습니다.');
            navigate('/me');

        } catch (error) {
            console.error('Payment Error', error);
            alert('결제 처리 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    if (!tradeId) {
        return <div className="text-center py-20">잘못된 접근입니다.</div>;
    }

    if (!paymentData) {
        return <div className="text-center py-20">결제 정보 로딩 중...</div>;
    }

    return (
        <div className="flex justify-center bg-gray-50 min-h-screen py-8">
            <div className="max-w-[600px] w-full bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col gap-6">
                <h2 className="text-2xl font-bold">결제</h2>


                {/* Order Summary */}
                <div className="flex flex-col gap-3 border-t border-gray-100 pt-6">
                    <h3 className="font-bold text-lg">최종 주문 정보</h3>
                    <div className="flex items-center gap-4">
                        <div className="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden flex items-center justify-center">
                            <span className="text-gray-400 text-[10px]">No Image</span>
                        </div>
                        <div className="flex flex-col">
                            <span className="font-bold text-sm">{paymentData.productName}</span>
                            <span className="text-gray-500 text-xs text-right">{paymentData.amount.toLocaleString()}원</span>
                        </div>
                    </div>
                </div>

                {/* Payment Breakdown */}
                <div className="flex flex-col gap-2 border-t border-gray-100 pt-6">
                    <div className="flex justify-between items-center text-sm">
                        <span className="text-gray-500">총 상품금액</span>
                        <span className="font-bold">{paymentData.amount.toLocaleString()}원</span>
                    </div>
                    <div className="flex justify-between items-center text-sm">
                        <span className="text-gray-500">검수비</span>
                        <span>무료</span>
                    </div>
                    <div className="flex justify-between items-center font-bold text-lg mt-4 pt-4 border-t border-gray-200">
                        <span>총 결제금액</span>
                        <span className="text-primary">{paymentData.amount.toLocaleString()}원</span>
                    </div>
                </div>

                {/* Payment Methods */}
                <div className="flex flex-col gap-3 pt-6">
                    <h3 className="font-bold text-lg">결제 방법</h3>
                    <div className="grid grid-cols-2 gap-3">
                        <button className="border border-black font-bold rounded-lg py-3 bg-black text-white">일반 결제</button>
                        <button className="border border-gray-200 rounded-lg py-3 text-gray-500">네이버 페이</button>
                    </div>
                </div>

                <div className="mt-4">
                    <button
                        onClick={handlePayment}
                        disabled={isLoading}
                        className={`w-full h-14 bg-primary text-white font-bold rounded-xl text-lg hover:bg-black/80 transition-colors ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
                    >
                        {isLoading ? '결제 처리중...' : `${paymentData.amount.toLocaleString()}원 결제하기`}
                    </button>
                </div>

            </div>
        </div>
    );
};

export default PaymentPage;
