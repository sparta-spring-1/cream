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
        // Initialize PortOne SDK
        const jquery = document.createElement("script");
        jquery.src = "https://code.jquery.com/jquery-1.12.4.min.js";
        const iamport = document.createElement("script");
        iamport.src = "https://cdn.iamport.kr/js/iamport.payment-1.1.8.js";
        document.head.appendChild(jquery);
        document.head.appendChild(iamport);

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
            document.head.removeChild(jquery);
            document.head.removeChild(iamport);
        }
    }, [tradeId, navigate]);

    const handlePayment = async () => {
        if (!paymentData || !tradeId) return;

        try {
            setIsLoading(true);

            // 1. Get Config (Store ID, Channel Key)
            const config = await paymentApi.getConfig();
            const { IMP } = window;
            IMP.init(config.storeId);

            // 2. Request Payment to PortOne
            IMP.request_pay({
                pg: 'html5_inicis', // or user selected PG
                pay_method: 'card',
                merchant_uid: paymentData.paymentId,
                name: paymentData.productName,
                amount: paymentData.amount,
                buyer_email: paymentData.email,
                buyer_name: paymentData.customerName,
                buyer_tel: paymentData.customerPhoneNumber,
                // buyer_addr: paymentData.address, // Added to DTO requirements
                // buyer_postcode: paymentData.zipCode
            }, async (rsp: any) => {
                if (rsp.success) {
                    try {
                        // 3. Complete Payment on Server
                        await paymentApi.complete(paymentData.id, {
                            impUid: rsp.imp_uid,
                            merchantUid: rsp.merchant_uid
                        });

                        alert('결제가 성공적으로 완료되었습니다.');
                        navigate('/me');
                    } catch (error) {
                        console.error(error);
                        alert('결제 검증에 실패했습니다.');
                    }
                } else {
                    alert(`결제에 실패하였습니다. 에러내용: ${rsp.error_msg}`);
                }
            });

        } catch (error) {
            console.error('Payment Error', error);
            alert('결제 준비 중 오류가 발생했습니다.');
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
                <h2 className="text-2xl font-bold">배송/결제</h2>

                {/* Delivery Info */}
                <div className="flex flex-col gap-3">
                    <div className="flex justify-between items-center">
                        <h3 className="font-bold text-lg">배송 주소</h3>
                        <button className="text-xs text-gray-500 underline">변경</button>
                    </div>
                    <div className="p-4 bg-gray-50 rounded-lg text-sm">
                        <p className="font-bold">{paymentData.customerName}</p>
                        <p className="text-gray-500">{paymentData.customerPhoneNumber}</p>
                        <p className="text-gray-500">{paymentData.email}</p>
                        {/* Address will be added later when DTO is updated */}
                    </div>
                </div>

                {/* Order Summary */}
                <div className="flex flex-col gap-3 border-t border-gray-100 pt-6">
                    <h3 className="font-bold text-lg">최종 주문 정보</h3>
                    <div className="flex items-center gap-4">
                        <div className="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden">
                            <img
                                src="https://placehold.co/200x200/f0f0f0/333333?text=Product"
                                alt="Product"
                                className="w-full h-full object-cover"
                            />
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
                    <div className="flex justify-between items-center text-sm">
                        <span className="text-gray-500">배송비</span>
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
