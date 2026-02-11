import { useEffect, useState } from 'react';
import { getSettlements, getSettlementDetails } from '../../api/settlement';
import type { SettlementListResponse, SettlementDetailsResponse } from '../../api/settlement';
import { paymentApi } from '../../api/payment';
import { ChevronRight, X } from 'lucide-react';

const MySettlementHistory = () => {
    const [settlements, setSettlements] = useState<SettlementListResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [selectedSettlement, setSelectedSettlement] = useState<SettlementDetailsResponse | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    useEffect(() => {
        fetchSettlements();
    }, []);

    const fetchSettlements = async () => {
        try {
            setLoading(true);
            const data = await getSettlements();
            setSettlements(data);
        } catch (err: any) {
            setError(err.response?.data?.message || '정산 내역을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleSettlementClick = async (id: number) => {
        try {
            const data = await getSettlementDetails(id);
            setSelectedSettlement(data);
            setIsModalOpen(true);
        } catch (err: any) {
            alert(err.response?.data?.message || '상세 정보를 불러오는데 실패했습니다.');
        }
    };

    const handleRefund = async () => {
        if (!selectedSettlement) return;

        const reason = prompt("환불 사유를 입력해주세요:");
        if (reason === null) return; // Cancelled
        if (!reason.trim()) {
            alert("환불 사유를 입력해야 합니다.");
            return;
        }

        if (!confirm(`정말로 환불하시겠습니까? \n금액: ${selectedSettlement.totalAmount.toLocaleString()}원`)) {
            return;
        }

        try {
            await paymentApi.refund(selectedSettlement.paymentId, {
                tradeId: selectedSettlement.tradeId,
                amount: selectedSettlement.totalAmount, // Full refund
                reason: reason
            });
            alert("환불이 완료되었습니다.");
            closeModal();
            fetchSettlements(); // Refresh list
        } catch (err: any) {
            console.error(err);
            alert(err.response?.data?.message || '환불 처리에 실패했습니다.');
        }
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setSelectedSettlement(null);
    };

    if (loading) return <div className="text-center py-10">로딩 중...</div>;
    if (error) return <div className="text-center py-10 text-red-500">{error}</div>;

    return (
        <div className="space-y-4">
            <h2 className="text-xl font-bold mb-4">정산 내역</h2>
            {settlements.length === 0 ? (
                <div className="text-center py-10 text-gray-500 bg-gray-50 rounded-lg">
                    정산 내역이 없습니다.
                </div>
            ) : (
                <div className="space-y-4">
                    {settlements.map((settlement) => (
                        <div
                            key={settlement.id}
                            onClick={() => handleSettlementClick(settlement.id)}
                            className="bg-white border rounded-lg p-4 flex justify-between items-center cursor-pointer hover:shadow-md transition-shadow"
                        >
                            <div>
                                <div className="font-semibold text-lg">{settlement.productName}</div>
                                <div className="text-sm text-gray-500">
                                    {settlement.status === 'COMPLETED' ? (
                                        <span className="text-green-600 font-bold">정산 완료</span>
                                    ) : (
                                        <span className="text-yellow-600 font-bold">정산 대기</span>
                                    )}
                                    <span className="mx-2">|</span>
                                    {settlement.settledAt ? new Date(settlement.settledAt).toLocaleDateString() : '-'}
                                </div>
                            </div>
                            <div className="flex items-center space-x-4">
                                <span className="font-bold text-lg">{settlement.settlementAmount.toLocaleString()}원</span>
                                <ChevronRight className="w-5 h-5 text-gray-400" />
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* 정산 상세 모달 */}
            {isModalOpen && selectedSettlement && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg w-full max-w-md p-6 relative">
                        <button
                            onClick={closeModal}
                            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
                        >
                            <X className="w-6 h-6" />
                        </button>

                        <h3 className="text-lg font-bold mb-6 border-b pb-2">정산 상세 정보</h3>

                        <div className="space-y-4">
                            <div className="flex justify-between">
                                <span className="text-gray-600">상품명</span>
                                <span className="font-medium text-right w-2/3">{selectedSettlement.productName}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-gray-600">상태</span>
                                <span className={`font-bold ${selectedSettlement.status === 'COMPLETED' ? 'text-green-600' : 'text-yellow-600'}`}>
                                    {selectedSettlement.status === 'COMPLETED' ? '정산 완료' : '정산 대기'}
                                </span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-gray-600">완료일</span>
                                <span>{selectedSettlement.settledAt ? new Date(selectedSettlement.settledAt).toLocaleString() : '-'}</span>
                            </div>
                            <div className="border-t pt-4 mt-4">
                                <div className="flex justify-between mb-2">
                                    <span className="text-gray-600">총 거래 금액</span>
                                    <span>{selectedSettlement.totalAmount.toLocaleString()}원</span>
                                </div>
                                <div className="flex justify-between mb-2 text-red-500">
                                    <span>수수료 (10%)</span>
                                    <span>-{selectedSettlement.feeAmount.toLocaleString()}원</span>
                                </div>
                                <div className="flex justify-between font-bold text-lg border-t pt-2 mt-2">
                                    <span>최종 지급액</span>
                                    <span>{selectedSettlement.settlementAmount.toLocaleString()}원</span>
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 flex flex-col gap-2">
                            {/* Refund Button - Only visible if COMPLETED? Or always allowed if policy permits? 
                                Assuming refund is possible on completed deals or pending ones for test/admin purposes.
                                But 'settlement' implies deal is done. 
                                Let's allow refund on 'PENDING' too if needed, but usually 'COMPLETED' means money is sent.
                                However, user asked for 'refund' feature here. Let's make it always visible or check status if logic requires.
                                For now, adding it unconditionally as requested for the feature.
                             */}
                            <button
                                onClick={handleRefund}
                                className="bg-red-500 text-white px-6 py-2 rounded hover:bg-red-600 w-full font-bold"
                            >
                                환불 요청
                            </button>
                            <button
                                onClick={closeModal}
                                className="bg-gray-200 text-gray-800 px-6 py-2 rounded hover:bg-gray-300 w-full"
                            >
                                닫기
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MySettlementHistory;
