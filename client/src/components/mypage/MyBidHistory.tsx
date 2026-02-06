import { useEffect, useState } from 'react';
import { bidApi, BidResponse } from '../../api/bid';

// Extended type for display if API returns more info, 
// but currently BidResponse has { id, price, status }
// We might need to assume or fetch more details if the API supports it.
// Looking at BidResponseDto.java: it returns userId, productId, price, type, status, createdAt...
// We should update the frontend type definition if we want to show product name etc.
// But for now we stick to what we have or infer.
// Wait, `getMyBids` calls `/v1/bids/me` which returns `Page<BidResponseDto>`.
// So the real response has more fields. Let's define them here or update types later.

interface MyBid extends BidResponse {
    productId: number;
    type: 'BUY' | 'SELL';
    createdAt: string;
    // Note: The API might NOT return product name. We might just show ID or price.
    // If backend doesn't return product name, UI will be limited.
    // BidResponseDto.java doesn't have productName.
    // So we can only show "Price" and "Type".
}

const MyBidHistory = () => {
    const [bids, setBids] = useState<MyBid[]>([]); // We cast the response
    const [isLoading, setIsLoading] = useState(true);

    const fetchBids = () => {
        setIsLoading(true);
        bidApi.getMyBids(0, 50) // Fetch first 50 for simplicity
            .then(data => {
                // data.content is the list
                setBids(data.content as MyBid[]);
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
                        {/* Backend doesn't give product name, showing ID is the best we can do without N+1 queries */}
                        <span className="text-xs text-gray-400">{new Date(bid.createdAt).toLocaleDateString()}</span>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                        <span className="font-bold">{bid.price.toLocaleString()}원</span>
                        <span className="text-xs text-gray-500">{bid.status}</span>
                        {bid.status === 'PROCESS' && ( // Assuming PROCESS means active/waiting
                            <button
                                onClick={() => handleCancel(bid.id)}
                                className="px-2 py-1 border border-gray-300 rounded text-xs hover:bg-gray-50"
                            >
                                취소
                            </button>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default MyBidHistory;
