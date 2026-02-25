import { useEffect, useState } from 'react';
import { bidApi, type BidResponse } from '../../api/bid';
import { tradeApi } from '../../api/trade';

interface MyBid extends BidResponse {
    productOptionId: number;
    productId: number;
    type: 'BUY' | 'SELL';
    createdAt: string;
}

const MyBidHistory = () => {
    const [bids, setBids] = useState<MyBid[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchBids = () => {
        setIsLoading(true);
        bidApi.getMyBids(0, 50)
            .then(data => {
                setBids(data as MyBid[]);
            })
            .catch(err => console.error("Failed to fetch bids", err))
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
            await tradeApi.cancelTrade(tradeId);
            alert("거래가 취소되었습니다.");
            fetchBids();
        } catch (error) {
            console.error(error);
            alert("거래 취소에 실패했습니다.");
        }
    };

    const handleEdit = async (bidId: number, currentPrice: number, productOptionId: number, type: 'BUY' | 'SELL') => {
        const newPriceStr = prompt("수정할 가격을 입력하세요:", currentPrice.toString());
        if (newPriceStr === null) return;

        const newPrice = Number(newPriceStr);
        if (isNaN(newPrice) || newPrice <= 0) {
            alert("올바른 가격을 입력해주세요.");
            return;
        }

        try {
            await bidApi.updateBid(bidId, {
                price: newPrice,
                productOptionId: productOptionId,
                type: type
            });
            alert("입찰 가격이 수정되었습니다.");
            fetchBids();
        } catch (error) {
            console.error(error);
            alert("입찰 수정에 실패했습니다.");
        }
    };

    if (isLoading) return <div className="py-10 text-center text-gray-500">로딩 중...</div>;

    if (bids.length === 0) {
        return (
            <div className="py-20 flex flex-col items-center justify-center border-t border-b border-gray-100">
                <p className="text-gray-500">입찰 내역이 없습니다.</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-4">
            {bids.map(bid => (
                <div key={bid.id} className="border border-gray-200 rounded-xl p-4 flex justify-between items-center">
                    <div className="flex flex-col gap-1">
                        <span className={`text-xs font-bold ${bid.type === 'SELL' ? 'text-green-600' : 'text-red-500'}`}>
                            {bid.type === 'SELL' ? '판매 입찰' : '구매 입찰'}
                        </span>
                        <span className="font-bold text-sm">상품 ID: {bid.productId}</span>
                        <span className="text-xs text-gray-400">
                            {bid.createdAt && new Date(bid.createdAt).getFullYear() > 1970
                                ? new Date(bid.createdAt).toLocaleDateString()
                                : '-'}
                        </span>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                        <span className="font-bold">{bid.price.toLocaleString()}원</span>
                        <span className="text-xs text-gray-500">{bid.status}</span>

                        {bid.status === 'PENDING' && (
                            <div className="flex gap-2">
                                <button
                                    onClick={() => handleEdit(bid.id, bid.price, bid.productOptionId, bid.type)}
                                    className="px-2 py-1 border border-gray-300 rounded text-xs hover:bg-gray-50"
                                >
                                    수정
                                </button>
                                <button
                                    onClick={() => handleCancel(bid.id)}
                                    className="px-2 py-1 border border-gray-300 rounded text-xs hover:bg-gray-50 text-red-500"
                                >
                                    취소
                                </button>
                            </div>
                        )}

                        {bid.status === 'MATCHED' && bid.tradeId && (
                            <div className="flex gap-2">
                                {bid.type === 'BUY' && (
                                    <button
                                        onClick={() => window.location.href = `/payment?tradeId=${bid.tradeId}`}
                                        className="px-2 py-1 border border-blue-200 bg-blue-50 text-blue-600 rounded text-xs hover:bg-blue-100 font-bold"
                                    >
                                        결제하기
                                    </button>
                                )}
                                <button
                                    onClick={() => handleTradeCancel(bid.tradeId!)}
                                    className="px-2 py-1 border border-red-200 bg-red-50 text-red-600 rounded text-xs hover:bg-red-100"
                                >
                                    체결 취소
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default MyBidHistory;
