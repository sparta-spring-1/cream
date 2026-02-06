import { useEffect, useState } from 'react';
import { Ban } from 'lucide-react';
import { adminApi, type AdminBidMonitoringItem } from '../../api/admin';

const AdminBidTab = () => {
    const [bids, setBids] = useState<AdminBidMonitoringItem[]>([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(false);

    const fetchBids = async () => {
        setIsLoading(true);
        try {
            const data = await adminApi.monitorBids(page);
            setBids(data.items);
            setTotal(data.paging.totalElements);
        } catch (error) {
            console.error(error);
            alert("입찰 목록을 불러오는데 실패했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchBids();
    }, [page]);

    const handleCancel = async (id: number) => {
        const reason = prompt("취소 사유를 입력하세요 (예: 관리자 직권 취소)");
        if (!reason) return;

        try {
            await adminApi.cancelBid(id, {
                reasonCode: "ADMIN_CANCEL",
                comment: reason
            });
            alert("입찰이 취소되었습니다.");
            fetchBids();
        } catch (error) {
            console.error(error);
            alert("입찰 취소에 실패했습니다.");
        }
    };

    return (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold">입찰 모니터링 (총 {total}건)</h2>
                <button
                    onClick={fetchBids}
                    className="px-3 py-1 bg-gray-100 rounded hover:bg-gray-200 text-sm"
                >
                    새로고침
                </button>
            </div>

            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
                <table className="w-full text-sm text-left">
                    <thead className="bg-gray-50 text-gray-500 font-medium border-b border-gray-200">
                        <tr>
                            <th className="px-4 py-3 w-16">ID</th>
                            <th className="px-4 py-3">타입</th>
                            <th className="px-4 py-3">상품명</th>
                            <th className="px-4 py-3">사용자</th>
                            <th className="px-4 py-3">가격</th>
                            <th className="px-4 py-3">상태</th>
                            <th className="px-4 py-3">등록일</th>
                            <th className="px-4 py-3 text-right">관리</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {isLoading ? (
                            <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
                        ) : bids.length === 0 ? (
                            <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">조회된 입찰 내역이 없습니다.</td></tr>
                        ) : (
                            bids.map(bid => (
                                <tr key={bid.bidId} className="hover:bg-gray-50">
                                    <td className="px-4 py-3 text-gray-500">{bid.bidId}</td>
                                    <td className="px-4 py-3">
                                        <span className={`px-2 py-1 rounded text-xs font-bold ${bid.type === 'BUY' ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'}`}>
                                            {bid.type === 'BUY' ? '구매' : '판매'}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3 font-medium truncate max-w-[200px]" title={bid.productName}>
                                        {bid.productName}
                                    </td>
                                    <td className="px-4 py-3 text-gray-600">{bid.userName}</td>
                                    <td className="px-4 py-3 font-medium">{bid.price.toLocaleString()}원</td>
                                    <td className="px-4 py-3">
                                        <span className="px-2 py-1 bg-gray-100 rounded text-xs text-gray-600">{bid.status}</span>
                                    </td>
                                    <td className="px-4 py-3 text-gray-400 text-xs">
                                        {new Date(bid.createdAt).toLocaleDateString()}
                                    </td>
                                    <td className="px-4 py-3 text-right">
                                        {bid.status !== 'CANCELLED' && bid.status !== 'COMPLETED' && (
                                            <button
                                                onClick={() => handleCancel(bid.bidId)}
                                                className="p-1 text-red-500 hover:bg-red-50 rounded flex items-center gap-1 ml-auto"
                                                title="강제 취소"
                                            >
                                                <Ban size={14} />
                                                <span className="text-xs">취소</span>
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <div className="flex justify-center gap-2 mt-4">
                <button
                    disabled={page === 0}
                    onClick={() => setPage(p => p - 1)}
                    className="px-3 py-1 border rounded disabled:opacity-50"
                >
                    이전
                </button>
                <span className="px-3 py-1">Page {page + 1}</span>
                <button
                    onClick={() => setPage(p => p + 1)}
                    className="px-3 py-1 border rounded"
                >
                    다음
                </button>
            </div>
        </div>
    );
};

export default AdminBidTab;
