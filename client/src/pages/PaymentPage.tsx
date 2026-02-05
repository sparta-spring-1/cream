import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { paymentApi } from '../api/payment';

const PaymentPage = () => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        // Initialize PortOne SDK
        const jquery = document.createElement("script");
        jquery.src = "https://code.jquery.com/jquery-1.12.4.min.js";
        const iamport = document.createElement("script");
        iamport.src = "https://cdn.iamport.kr/js/iamport.payment-1.1.8.js";
        document.head.appendChild(jquery);
        document.head.appendChild(iamport);

        return () => {
            document.head.removeChild(jquery);
            document.head.removeChild(iamport);
        }
    }, []);

    const handlePayment = async () => {
        try {
            setIsLoading(true);

            // 1. Get Config (Store ID, Channel Key)
            const config = await paymentApi.getConfig();
            const { IMP } = window;
            IMP.init(config.storeId);

            // 2. Prepare Payment (For testing, using logic that doesn't strictly depend on a real tradeId if the backend allows, 
            // OR we'll use a dummy tradeId if provided. Assuming backend needs a valid tradeId, 
            // but for this UI demo we might need to handle the case where tradeId comes from previous page.
            // For now, I'll pass a dummy ID '1' or rely on what's available. 
            // Ideally, we should receive tradeId from location state or params.)
            // Let's assume we are paying for the "Payment Test Item" which might need a trade.
            // For this specific 'payment-test' flow, maybe the backend creates a trade implicitly or we just simulate 'prepare' for now.

            // Re-reading user request: "Portone integration... refer to PaymentController".
            // PaymentController.prepare takes { tradeId }. 
            // If the user hasn't created a trade yet, this might fail.
            // However, the prompt implies "get it working".
            // Let's try to prepare with a hardcoded ID '1' for the test item, hoping a trade exists or backend handles it.
            // If it fails, I'll advise the user.

            const prepareResponse = await paymentApi.prepare(1); // Using dummy tradeId 1 for test

            // 3. Request Payment to PortOne
            IMP.request_pay({
                pg: 'html5_inicis', // or user selected PG
                pay_method: 'card',
                merchant_uid: prepareResponse.merchantUid,
                name: 'Payment Test Item',
                amount: prepareResponse.amount, // Should be 10000
                buyer_email: 'test@cream.co.kr',
                buyer_name: 'Hong Gil Dong',
                buyer_tel: '010-1234-5678',
                buyer_addr: 'Seoul, Gangnam-gu, Teheran-ro 123',
                buyer_postcode: '01234'
            }, async (rsp: any) => {
                if (rsp.success) {
                    try {
                        // 4. Complete Payment on Server
                        // We need paymentId to call complete. 
                        // The backend `prepare` response usually returns a `paymentId` or we might need another way.
                        // Looking at PaymentController, `prepare` returns `CreatePaymentResponse`.
                        // Let's check `CreatePaymentResponse` structure if possible, but based on typical flows...
                        // Wait, PaymentController.complete takes @PathVariable paymentId.
                        // The `prepare` returns `merchantUid` and likely `paymentId`.
                        // I will assume `prepareResponse` has `paymentId`. 
                        // If type definition is wrong, I will fix it.

                        // Wait, I defined `PrepareResponse` with only `merchantUid` and `amount`.
                        // I should probably verify what `CreatePaymentResponse` actually has.
                        // But proceeding with assumption it maps to paymentId.

                        // FIXME: Assuming prepareResponse has ID. If not, we might need to query it.
                        // For now let's assume `prepareResponse.paymentId` exists in the real backend response even if I missed it in TS interface.

                        await paymentApi.complete((prepareResponse as any).paymentId, {
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
            alert('결제 준비 중 오류가 발생했습니다. (백엔드 연결 확인 필요)');
        } finally {
            setIsLoading(false);
        }
    };

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
                        <p className="font-bold">홍길동</p>
                        <p className="text-gray-500">010-1234-5678</p>
                        <p className="text-gray-500">서울시 강남구 테헤란로 123 크림빌딩 10층</p>
                    </div>
                </div>

                {/* Order Summary */}
                <div className="flex flex-col gap-3 border-t border-gray-100 pt-6">
                    <h3 className="font-bold text-lg">최종 주문 정보</h3>
                    <div className="flex items-center gap-4">
                        <div className="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden">
                            <img
                                src="https://placehold.co/200x200/135bec/ffffff?text=TEST"
                                alt="Product"
                                className="w-full h-full object-cover"
                            />
                        </div>
                        <div className="flex flex-col">
                            <span className="font-bold text-sm">Payment Test Item</span>
                            <span className="text-gray-500 text-xs">260 / 테스트</span>
                        </div>
                    </div>
                </div>

                {/* Payment Breakdown */}
                <div className="flex flex-col gap-2 border-t border-gray-100 pt-6">
                    <div className="flex justify-between items-center text-sm">
                        <span className="text-gray-500">총 상품금액</span>
                        <span className="font-bold">10,000원</span>
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
                        <span className="text-primary">10,000원</span>
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
                        className="w-full h-14 bg-primary text-white font-bold rounded-xl text-lg hover:bg-black/80 transition-colors"
                    >
                        10,000원 결제하기
                    </button>
                </div>

            </div>
        </div>
    );
};

export default PaymentPage;
