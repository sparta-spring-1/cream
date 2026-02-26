import { useEffect, useState } from 'react';
import { bidApi } from '../../api/bid';
import { tradeApi } from '../../api/trade';
import { productApi } from '../../api/product';
import { useNavigate } from 'react-router-dom';

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
interface MyTrade {
    id: number;
    productName: string;
    size: string;
    price: number;
    status: string;
    matchedAt: string;
    role: 'BUYER' | 'SELLER';
}

const MyBidHistory = () => {
    const [activeTab, setActiveTab] = useState<'BID' | 'TRADE'>('BID');
    const [bids, setBids] = useState<MyBid[]>([]);
    const [trades, setTrades] = useState<MyTrade[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editingBid, setEditingBid] = useState<MyBid | null>(null);
    const [editPrice, setEditPrice] = useState('');
    const [options, setOptions] = useState<{ id: number, size: string }[]>([]);
    const [selectedOptionId, setSelectedOptionId] = useState<number | null>(null);

    const fetchBids = () => {
        setIsLoading(true);
        bidApi.getMyBids(0, 50)
            .then(data => {
                // 1. 데이터가 Page 객체인지 배열인지 확인
                const list = data.content || data || [];

                const formatted = list.map((bid: any) => {
                    // 백엔드에서 SALE로 오든 SELL로 오든 SELL로 통일
                    const rawType = String(bid.type || "").toUpperCase();
                    const normalizedType = (rawType === 'SALE' || rawType === 'SELL') ? 'SELL' : 'BUY';

                    return {
                        ...bid,
                        productId: bid.productId, // DTO에 있는 필드 그대로 사용
                        type: normalizedType,
                        status: String(bid.status || "").toUpperCase()
                    };
                });

                setBids(formatted);
            })
            .catch(err => console.error("데이터 로드 실패:", err))
            .finally(() => setIsLoading(false));
    }

    const fetchTrades = async () => {
        setIsLoading(true);
        try {
            const data = await tradeApi.getMyTrades(0, 50);
            setTrades(data.content || []);
        } catch (err) {
            console.error("체결 로드 실패:", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (activeTab === 'BID') fetchBids();
        else fetchTrades();
    }, [activeTab]);


    const handleCancelBid = async (bidId: number) => {
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

    const handleCancelTrade = async (tradeId: number) => {
        if (!confirm("체결된 거래를 취소하시겠습니까? 취소 시 3일간 입찰이 제한되는 패널티가 부과됩니다.")) return;
        try {
            await tradeApi.cancelTrade(tradeId);
            alert("거래가 취소되었습니다. 패널티가 적용됩니다.");
            fetchTrades();
        } catch (error: any) {
            alert(error.response?.data?.message || "거래 취소 실패");
        }
    };

    const openEditModal = async (bid: MyBid) => {
        setEditingBid(bid);
        setEditPrice(bid.price.toString());

        try {
            const productData = await productApi.getPublicProduct(bid.productId);
            if (productData && productData.options && productData.options.length > 0) {
                setOptions(productData.options);
                const matched = productData.options.find((o: { id: number }) => o.id === bid.productOptionId);
                setSelectedOptionId(matched ? matched.id : productData.options[0].id);
            } else {
                setSelectedOptionId(bid.productOptionId ?? null);
            }
        } catch (err) {
            console.error("Failed to fetch product options", err);
            setOptions([{id: bid.productOptionId, size: '현재 선택된 사이즈'}]);
            setSelectedOptionId(bid.productOptionId ?? null);
        }
        setIsEditModalOpen(true);
    };

    const handleEditSubmit = async () => {
        if (!editingBid) return;

        // 1. 가격 결정: 입력값이 있으면 그 값을 쓰고, 없으면 기존 가격 사용
        // trim()을 추가하여 공백 문자열 방지
        const currentInputPrice = editPrice.trim();
        const finalPrice = currentInputPrice !== "" ? Number(currentInputPrice) : editingBid.price;

        // 2. 옵션 결정: 선택된 값이 있으면 쓰고, 없으면 기존 옵션 ID 사용
        const finalOptionId = (selectedOptionId !== null && selectedOptionId !== undefined)
            ? selectedOptionId
            : editingBid.productOptionId;

        // 디버깅 로그: 브라우저 콘솔에서 확인용
        console.log("최종 제출 데이터:", {
            finalPrice,
            finalOptionId,
            originalBid: editingBid
        });

        // 3. 누락 체크 로직 수정 (finalPrice가 0일 수도 있으므로 엄격하게 체크)
        if (finalPrice === undefined || finalPrice === null || !finalOptionId) {
            alert("가격 또는 옵션 정보가 유효하지 않습니다.");
            return;
        }

        try {
            await bidApi.updateBid(editingBid.id, {
                price: finalPrice,
                productOptionId: finalOptionId,
                type: editingBid.type as 'BUY' | 'SELL'
            });

            alert("입찰 정보가 수정되었습니다.");
            setIsEditModalOpen(false);
            fetchBids(); // 목록 새로고침
        } catch (error: any) {
            console.error("수정 실패:", error);
            const errorMsg = error.response?.data?.message || "수정 처리 중 오류가 발생했습니다.";
            alert(errorMsg);
        }
    };

    if (isLoading) return <div className="py-10 text-center text-gray-500">로딩 중...</div>;

    return (
        <div className="flex flex-col gap-4 max-w-lg mx-auto">
            {/* --- 상단 탭 UI --- */}
            <div className="flex border-b border-gray-200">
                <button
                    onClick={() => setActiveTab('BID')}
                    className={`flex-1 py-4 font-bold text-sm transition-all ${activeTab === 'BID' ? 'border-b-2 border-black text-black' : 'text-gray-400'}`}
                >
                    입찰 내역 ({bids.length})
                </button>
                <button
                    onClick={() => setActiveTab('TRADE')}
                    className={`flex-1 py-4 font-bold text-sm transition-all ${activeTab === 'TRADE' ? 'border-b-2 border-black text-black' : 'text-gray-400'}`}
                >
                    체결 내역 ({trades.length})
                </button>
            </div>

            {/* --- 리스트 영역 --- */}
            <div className="mt-2 space-y-4">
                {activeTab === 'BID' ? (
                    bids.map(bid => (
                        <div key={bid.id} className="border border-gray-200 rounded-xl p-4 flex justify-between items-center bg-white shadow-sm">
                            <div className="flex flex-col gap-1">
                                <span className={`text-xs font-bold ${bid.type === 'SELL' ? 'text-green-600' : 'text-red-500'}`}>
                                    {bid.type === 'SELL' ? '판매 입찰' : '구매 입찰'}
                                </span>
                                <span className="font-bold text-sm">상품 ID: {bid.productId}</span>
                                <span className="text-[10px] text-gray-400">{new Date(bid.createdAt).toLocaleString()}</span>
                            </div>
                            <div className="flex flex-col items-end gap-2">
                                <span className="font-bold text-lg">{bid.price.toLocaleString()}원</span>
                                {bid.status === 'PENDING' && (
                                    <div className="flex gap-2">
                                        <button onClick={() => openEditModal(bid)} className="px-3 py-1.5 border border-gray-300 rounded-md text-xs font-bold hover:bg-gray-50">수정</button>
                                        <button onClick={() => handleCancelBid(bid.id)} className="px-3 py-1.5 border border-gray-300 rounded-md text-xs font-bold text-red-500 hover:bg-red-50">취소</button>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))
                ) : (
                    trades.map(trade => (
                        <div key={trade.id} className="border border-gray-200 rounded-xl p-4 flex justify-between items-center bg-white shadow-sm">
                            <div className="flex flex-col gap-1">
                                <span className={`px-2 py-0.5 w-fit rounded text-[10px] font-bold ${trade.role === 'SELLER' ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'}`}>
                                    {trade.role === 'SELLER' ? '판매 체결' : '구매 체결'}
                                </span>
                                <span className="font-bold text-sm truncate max-w-[150px]">{trade.productName}</span>
                                <span className="text-xs text-gray-500">사이즈: {trade.size}</span>
                                <span className="text-[10px] text-gray-400">{new Date(trade.matchedAt).toLocaleString()}</span>
                            </div>
                            <div className="flex flex-col items-end gap-2">
                                <span className="font-bold text-lg">{trade.price.toLocaleString()}원</span>
                                <div className="flex gap-2">
                                    {trade.role === 'BUYER' && trade.status === 'WAITING_PAYMENT' && (
                                        <button
                                            onClick={() => navigate(`/payment?tradeId=${trade.id}`)}
                                            className="px-3 py-1.5 bg-blue-500 text-white rounded-md text-xs font-bold hover:bg-blue-600 transition-all"
                                        >
                                            결제하기
                                        </button>
                                    )}
                                    {trade.status === 'WAITING_PAYMENT' && (
                                        <button
                                            onClick={() => handleCancelTrade(trade.id)}
                                            className="px-3 py-1.5 bg-red-50 text-red-600 border border-red-200 rounded-md text-xs font-bold hover:bg-red-100"
                                        >
                                            체결 취소
                                        </button>
                                    )}
                                    {trade.status !== 'WAITING_PAYMENT' && (
                                        <span className="text-xs font-bold text-gray-400 bg-gray-50 px-2 py-1 rounded">
                                            {trade.status === 'PAYMENT_COMPLETED' ? '결제 완료' : '거래 종료'}
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>

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
