import { useEffect, useState } from 'react';
import { bidApi } from '../../api/bid';
import { tradeApi } from '../../api/trade';
import { productApi } from '../../api/product';

interface MyBid {
    id: number;
    price: number;
    type: string;
    status: string;
    createdAt: string;
    productId: number;
    productOptionId: number;
    tradeId?: number;
    sizeName?: string;
}

const MyBidHistory = () => {
    const [bids, setBids] = useState<MyBid[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editingBid, setEditingBid] = useState<MyBid | null>(null);
    const [editPrice, setEditPrice] = useState('');
    const [options, setOptions] = useState<{ id: number, size: string }[]>([]);
    const [selectedOptionId, setSelectedOptionId] = useState<number | null>(null);

    const fetchBids = () => {
        setIsLoading(true);
        bidApi.getMyBids(0, 50)
            .then(data => {

                const formatted = (data.content as any[]).map(bid => ({
                    ...bid,
                    type: bid.type?.trim().toUpperCase(),
                    status: bid.status?.trim().toUpperCase()
                }));
                setBids(formatted);

            })
            .catch(err => console.error(err))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        fetchBids();
    }, []);

    const handleCancel = async (bidId: number) => {
        if (!confirm("입찰을 취소하시겠습니까?")) return;
        try {
            await bidApi.cancelBid(bidId);
            alert("입찰이 취소되었습니다.");
            fetchBids();
        } catch (error) {
            console.error(error);
            alert("입찰 취소에 실패했습니다.");
        }
    };

    const handleTradeCancel = async (tradeId: number) => {
        if (!confirm("체결된 거래를 취소하시겠습니까? 취소 시 3일간 입찰이 제한됩니다.")) return;
        try {
            const response = await tradeApi.cancelTrade(tradeId);
            alert(response.message || "거래가 취소되었습니다. 3일간 입찰이 제한됩니다.");
            fetchBids();
        } catch (error: any) {
            console.error(error);
            alert(error.response?.data?.message || "거래 취소에 실패했습니다.");
        }
    };

    const openEditModal = async (bid: MyBid) => {
        setEditingBid(bid);
        setEditPrice(bid.price.toString());
        setSelectedOptionId(bid.productOptionId);

        try {
            const productData = await productApi.getPublicProduct(bid.productId);
            if (productData && productData.options) {
                setOptions(productData.options);
            }
        } catch (err) {
            console.error("Failed to fetch product options", err);
            setOptions([{id: bid.productOptionId, size: '현재 선택된 사이즈'}]);
        }
        setIsEditModalOpen(true);
    };

    const handleEditSubmit = async () => {
        if (!editingBid || !selectedOptionId) return;

        try {
            await bidApi.updateBid(editingBid.id, {
                price: Number(editPrice),
                productOptionId: selectedOptionId,
                type: editingBid.type as 'BUY' | 'SELL'
            });

            alert("입찰 정보가 수정되었습니다.");
            setIsEditModalOpen(false);
            fetchBids();
        } catch (error: any) {
            const errorMsg = error.response?.data?.message || "입력값이 유효하지 않습니다.";
            alert(`수정 실패: ${errorMsg}`);
        }
    };

    if (isLoading) return <div className="py-10 text-center text-gray-500">로딩 중...</div>;

    return (
        <div className="flex flex-col gap-4 relative">
            {bids.map(bid => {
                const isSell = bid.type === 'SELL';
                const isPending = bid.status === 'PENDING';

                return (
                    <div key={bid.id}
                         className="border border-gray-200 rounded-xl p-4 flex justify-between items-center bg-white shadow-sm">
                        <div className="flex flex-col gap-1">
                        <span className={`text-xs font-bold ${isSell ? 'text-green-600' : 'text-red-500'}`}>
                            {isSell ? '판매 입찰' : '구매 입찰'}
                        </span>
                            <span className="font-bold text-sm">상품 ID: {bid.productId}</span>
                            <span
                                className="text-[10px] text-gray-400">{new Date(bid.createdAt).toLocaleString()}</span>
                        </div>

                        <div className="flex flex-col items-end gap-2">
                            <span className="font-bold text-lg">{bid.price.toLocaleString()}원</span>

                            {isPending && (
                                <div className="flex gap-2">
                                    <button onClick={() => openEditModal(bid)}
                                            className="px-3 py-1.5 border border-gray-300 rounded-md text-xs font-bold hover:bg-gray-50 transition-all">
                                        수정
                                    </button>
                                    <button onClick={() => handleCancel(bid.id)}
                                            className="px-3 py-1.5 border border-gray-300 rounded-md text-xs font-bold text-red-500 hover:bg-red-50">
                                        취소
                                    </button>
                                </div>
                            )}

                            {bid.status === 'MATCHED' && bid.tradeId && (
                                <button onClick={() => handleTradeCancel(bid.tradeId!)}
                                        className="px-3 py-1.5 bg-red-50 text-red-600 border border-red-200 rounded-md text-xs font-bold">
                                    체결 취소
                                </button>
                            )}
                        </div>
                    </div>
                );
            })}

            {/* --- 단일 수정 모달 --- */}
            {isEditModalOpen && editingBid && (
                <div
                    className="fixed inset-0 bg-black/60 flex items-center justify-center z-[1000] p-4 backdrop-blur-sm">
                    <div className="bg-white w-full max-w-[400px] rounded-2xl p-8 shadow-2xl">
                        <h3 className="text-xl font-bold mb-6 text-center text-gray-800">입찰 정보 수정</h3>

                        {/* [수정 완료] 클릭 가능한 타입 선택 버튼 */}
                        <div className="flex gap-2 mb-6">
                            <button
                                type="button"
                                onClick={() => setEditingBid({...editingBid, type: 'BUY'})}
                                className={`flex-1 p-3 rounded-xl border-2 text-center transition-all ${
                                    editingBid.type === 'BUY'
                                        ? 'border-[#ef6253] bg-red-50 opacity-100'
                                        : 'border-transparent bg-gray-50 opacity-40 hover:opacity-60'
                                }`}
                            >
                                <span
                                    className={`font-bold ${editingBid.type === 'BUY' ? 'text-[#ef6253]' : 'text-gray-400'}`}>구매 입찰</span>
                            </button>
                            <button
                                type="button"
                                onClick={() => setEditingBid({...editingBid, type: 'SELL'})}
                                className={`flex-1 p-3 rounded-xl border-2 text-center transition-all ${
                                    editingBid.type === 'SELL'
                                        ? 'border-[#41b979] bg-green-50 opacity-100'
                                        : 'border-transparent bg-gray-50 opacity-40 hover:opacity-60'
                                }`}
                            >
                                <span
                                    className={`font-bold ${editingBid.type === 'SELL' ? 'text-[#41b979]' : 'text-gray-400'}`}>판매 입찰</span>
                            </button>
                        </div>

                        {/* 사이즈 선택 */}
                        <div className="space-y-2 mb-6">
                            <label className="text-xs font-bold text-gray-500 ml-1">사이즈 선택</label>
                            <select
                                value={selectedOptionId || ''}
                                onChange={(e) => setSelectedOptionId(Number(e.target.value))}
                                className="w-full p-4 bg-gray-50 border-2 border-gray-100 rounded-xl font-bold text-sm focus:border-black outline-none transition-all cursor-pointer"
                            >
                                {options.map(opt => (
                                    <option key={opt.id} value={opt.id}>{opt.size}</option>
                                ))}
                                {options.length === 0 && <option value={editingBid.productOptionId}>현재 옵션 유지</option>}
                            </select>
                        </div>

                        {/* 가격 입력 */}
                        <div className="space-y-2 mb-8">
                            <label className="text-xs font-bold text-gray-500 ml-1">수정 가격 (원)</label>
                            <div className="relative">
                                <input
                                    type="text"
                                    value={editPrice}
                                    onChange={(e) => setEditPrice(e.target.value.replace(/[^0-9]/g, ''))}
                                    className="w-full p-4 bg-gray-50 rounded-xl text-right font-bold text-xl outline-none border-2 border-transparent focus:border-black transition-all"
                                    autoFocus
                                />
                                <span
                                    className="absolute left-4 top-1/2 -translate-y-1/2 font-bold text-gray-400">₩</span>
                            </div>
                        </div>

                        <div className="flex gap-3">
                            <button onClick={() => setIsEditModalOpen(false)}
                                    className="flex-1 py-4 bg-gray-100 rounded-xl font-bold text-gray-500 hover:bg-gray-200 transition-colors">취소
                            </button>
                            <button
                                onClick={handleEditSubmit}
                                className={`flex-1 py-4 text-white rounded-xl font-bold shadow-lg transition-all active:scale-95 ${editingBid.type === 'SELL' ? 'bg-[#41b979]' : 'bg-[#ef6253]'}`}
                            >
                                수정 완료
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default MyBidHistory;
