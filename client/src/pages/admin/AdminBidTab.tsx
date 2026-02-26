import { useEffect, useState } from 'react';
import { Ban, X } from 'lucide-react';
import { adminApi, type AdminBidMonitoringItem } from '../../api/admin';

const CANCEL_REASONS = [
    { code: 'FRAUD', label: '매크로 및 부정거래 의심' },
    { code: 'OUT_OF_STOCK', label: '상품 재고 없음' },
    { code: 'MISTAKE', label: '가격 오기입' },
    { code: 'POLICY_VIOLATION', label: '운영 정책 위반' }
];

const AdminBidTab = () => {
    const [bids, setBids] = useState<AdminBidMonitoringItem[]>([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(false);


    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedBidId, setSelectedBidId] = useState<number | null>(null);
    const [cancelForm, setCancelForm] = useState({ reasonCode: 'FRAUD', comment: '' });

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

    const openCancelModal = (id: number) => {
        setSelectedBidId(id);
        setCancelForm({ reasonCode: 'FRAUD', comment: '' }); // 초기값 설정
        setIsModalOpen(true);
    };

    const handleAdminCancelSubmit = async () => {
        if (!selectedBidId) return;
        if (!cancelForm.comment.trim()) {
            alert("상세 코멘트를 입력해주세요.");
            return;
        }

        try {
            await adminApi.cancelBid(selectedBidId, {
                reasonCode: cancelForm.reasonCode,
                comment: cancelForm.comment
            });
            alert("관리자 권한으로 취소 처리되었습니다.");
            setIsModalOpen(false);
            fetchBids();
        } catch (error: any) {
            console.error(error);
            alert(error.response?.data?.message || "취소 처리에 실패했습니다.");
        }
    };

    return (

        <div className="relative">
            <div className="flex justify-between items-center mb-4">
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
                                    {bid.status === 'PENDING' && (
                                        <button
                                            onClick={() => openCancelModal(bid.bidId)}
                                            className="p-1 text-red-500 hover:bg-red-50 rounded flex items-center gap-1 ml-auto"
                                            title="강제 취소"
                                        >
                                            <Ban size={14} />
                                            <span className="text-xs font-bold">취소</span>
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            {/* 페이지네이션 */}
            <div className="flex justify-center gap-2 mt-4">
                <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="px-3 py-1 border rounded disabled:opacity-50 hover:bg-gray-50">이전</button>
                <span className="px-3 py-1 font-medium">Page {page + 1}</span>
                <button onClick={() => setPage(p => p + 1)} className="px-3 py-1 border rounded hover:bg-gray-50">다음</button>
            </div>

            {/* --- 관리자 전용 취소 모달 UI --- */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-[1100] p-4 backdrop-blur-sm">
                    <div className="bg-white w-full max-w-md rounded-2xl p-6 shadow-2xl animate-in fade-in zoom-in duration-200">
                        <div className="flex justify-between items-center mb-5">
                            <h3 className="text-lg font-bold text-gray-900">입찰 강제 취소</h3>
                            <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600 transition-colors"><X size={24} /></button>
                        </div>

                        <div className="space-y-5">
                            {/* 사유 선택 (Enum 매칭) */}
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-2">취소 사유 선택</label>
                                <select
                                    className="w-full border-2 border-gray-100 rounded-xl p-3 text-sm focus:border-black outline-none transition-all cursor-pointer"
                                    value={cancelForm.reasonCode}
                                    onChange={(e) => setCancelForm({ ...cancelForm, reasonCode: e.target.value })}
                                >
                                    {CANCEL_REASONS.map(reason => (
                                        <option key={reason.code} value={reason.code}>{reason.label} ({reason.code})</option>
                                    ))}
                                </select>
                            </div>

                            {/* 상세 메모 */}
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-2">상세 코멘트</label>
                                <textarea
                                    className="w-full border-2 border-gray-100 rounded-xl p-3 text-sm min-h-[100px] outline-none focus:border-black transition-all resize-none"
                                    placeholder="취소에 대한 구체적인 근거를 입력하세요."
                                    value={cancelForm.comment}
                                    onChange={(e) => setCancelForm({ ...cancelForm, comment: e.target.value })}
                                />
                            </div>

                            <div className="flex gap-3 pt-2">
                                <button onClick={() => setIsModalOpen(false)} className="flex-1 py-4 bg-gray-100 rounded-xl font-bold text-gray-500 hover:bg-gray-200 transition-colors">닫기</button>
                                <button
                                    onClick={handleAdminCancelSubmit}
                                    className="flex-1 py-4 bg-red-500 text-white rounded-xl font-bold shadow-lg hover:bg-red-600 active:scale-95 transition-all"
                                >
                                    취소 확정
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminBidTab;
