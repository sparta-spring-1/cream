import { useEffect, useState } from 'react';
import { Ban } from 'lucide-react';
import { adminApi, type AdminBidMonitoringItem } from '../../api/admin';

const AdminBidTab = () => {
    const [bids, setBids] = useState<AdminBidMonitoringItem[]>([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(false);

    // Filters
    const [status, setStatus] = useState('');
    const [type, setType] = useState('');
    const [productId, setProductId] = useState<number | undefined>(undefined);

    const fetchBids = async () => {
        setIsLoading(true);
        try {
            const data = await adminApi.monitorBids(page, {
                status: status || undefined,
                type: type || undefined,
                productId: productId || undefined
            });
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
    }, [page, status, type, productId]);

    const handleCancel = async (id: number) => {
        const reasonCode = prompt("취소 사유 코드를 입력하세요 (예: ADMIN_CANCEL, ABUSIVE_BID)", "ADMIN_CANCEL");
        if (!reasonCode) return;

        const comment = prompt("취소 상세 사유를 입력하세요");
        if (!comment) return;

        try {
            await adminApi.cancelBid(id, {
                reasonCode: reasonCode,
                comment: comment
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
            <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-bold">입찰 모니터링 (총 {total}건)</h2>
                <div className="flex gap-2">
                    <select
                        value={type}
                        onChange={(e) => { setType(e.target.value); setPage(0); }}
                        className="px-3 py-2 border rounded-lg text-sm outline-none bg-white"
                    >
                        <option value="">전체 타입</option>
                        <option value="BUY">구매</option>
                        <option value="SALE">판매</option>
                    </select>
                    <select
                        value={status}
                        onChange={(e) => { setStatus(e.target.value); setPage(0); }}
                        className="px-3 py-2 border rounded-lg text-sm outline-none bg-white"
                    >
                        <option value="">전체 상태</option>
                        <option value="PENDING">PENDING</option>
                        <option value="MATCHED">MATCHED</option>
                        <option value="CANCELLED">CANCELLED</option>
                    </select>
                    <input
                        type="number"
                        placeholder="상품 ID"
                        value={productId || ''}
                        onChange={(e) => { setProductId(e.target.value ? Number(e.target.value) : undefined); setPage(0); }}
                        className="w-24 px-3 py-2 border rounded-lg text-sm outline-none"
                    />
                    <button
                        onClick={() => { setType(''); setStatus(''); setProductId(undefined); setPage(0); }}
                        className="px-3 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 text-sm font-medium"
                    >
                        초기화
                    </button>
                </div>
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
