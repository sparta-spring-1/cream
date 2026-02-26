import { useEffect, useState } from 'react';
import { adminApi, type AdminTradeMonitoringItem } from '../../api/admin';

const AdminTradeTab = () => {
    const [trades, setTrades] = useState<AdminTradeMonitoringItem[]>([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(false);

    const fetchTrades = async () => {
        setIsLoading(true);
        try {
            const data = await adminApi.monitorTrades(page);
            setTrades(data.items);
            setTotal(data.paging.totalElements);
        } catch (error) {
            console.error(error);
            alert("거래 목록을 불러오는데 실패했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchTrades();
    }, [page]);

    return (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold">체결 모니터링 (총 {total}건)</h2>
                <button
                    onClick={fetchTrades}
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
                            <th className="px-4 py-3">상품명</th>
                            <th className="px-4 py-3">가격</th>
                            <th className="px-4 py-3">상태</th>
                            <th className="px-4 py-3">판매자</th>
                            <th className="px-4 py-3">구매자</th>
                            <th className="px-4 py-3">체결일</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {isLoading ? (
                            <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
                        ) : trades.length === 0 ? (
                            <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">조회된 체결 내역이 없습니다.</td></tr>
                        ) : (
                            trades.map(trade => (
                                <tr key={trade.tradeId} className="hover:bg-gray-50">
                                    <td className="px-4 py-3 text-gray-500">{trade.tradeId}</td>
                                    <td className="px-4 py-3 font-medium truncate max-w-[200px]" title={trade.productName}>
                                        {trade.productName}
                                    </td>
                                    <td className="px-4 py-3 font-bold">{trade.price.toLocaleString()}원</td>
                                    <td className="px-4 py-3">
                                        <span className={`px-2 py-1 rounded text-xs font-bold 
                                          ${trade.status === 'WAITING_PAYMENT' ? 'bg-yellow-50 text-yellow-600' : 
                                            trade.status === 'PAYMENT_COMPLETED' ? 'bg-blue-50 text-blue-600' :
                                            trade.status === 'PAYMENT_CANCELED' ? 'bg-red-50 text-red-600' : 
                                                'bg-gray-100 text-gray-600'}`}>
                                            {/* 백엔드 Enum 값을 보기 좋은 한글로 변환 */}
                                            {trade.status === 'WAITING_PAYMENT' ? '결제대기' :
                                                trade.status === 'PAYMENT_COMPLETED' ? '체결완료' :
                                                    trade.status === 'PAYMENT_CANCELED' ? '거래취소' : trade.status}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3 text-gray-600">{trade.sellerName}</td>
                                    <td className="px-4 py-3 text-gray-600">{trade.buyerName}</td>
                                    <td className="px-4 py-3 text-gray-400 text-xs">
                                        {new Date(trade.createdAt).toLocaleDateString()}
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

export default AdminTradeTab;
