import { useState } from 'react';
import { LayoutDashboard, ShoppingBag, TrendingUp, Gavel } from 'lucide-react';
import AdminProductTab from './admin/AdminProductTab';
import AdminBidTab from './admin/AdminBidTab';
import AdminTradeTab from './admin/AdminTradeTab';

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState<'product' | 'bid' | 'trade'>('product');

    return (
        <div className="flex justify-center bg-gray-50 min-h-screen py-8">
            <div className="max-w-[1200px] w-full px-4 flex flex-col gap-6">
                <div className="flex items-center gap-2">
                    <LayoutDashboard className="text-black" />
                    <h1 className="text-2xl font-bold">관리자 대시보드</h1>
                </div>

                <div className="bg-white rounded-xl shadow-sm border border-gray-200 min-h-[600px] flex flex-col">
                    {/* Tabs */}
                    <div className="flex border-b border-gray-200">
                        <button
                            onClick={() => setActiveTab('product')}
                            className={`flex items-center gap-2 px-6 py-4 font-medium transition-colors ${activeTab === 'product' ? 'text-black border-b-2 border-black' : 'text-gray-400 hover:text-gray-600'
                                }`}
                        >
                            <ShoppingBag size={18} />
                            상품 관리
                        </button>
                        <button
                            onClick={() => setActiveTab('bid')}
                            className={`flex items-center gap-2 px-6 py-4 font-medium transition-colors ${activeTab === 'bid' ? 'text-black border-b-2 border-black' : 'text-gray-400 hover:text-gray-600'
                                }`}
                        >
                            <Gavel size={18} />
                            입찰 모니터링
                        </button>
                        <button
                            onClick={() => setActiveTab('trade')}
                            className={`flex items-center gap-2 px-6 py-4 font-medium transition-colors ${activeTab === 'trade' ? 'text-black border-b-2 border-black' : 'text-gray-400 hover:text-gray-600'
                                }`}
                        >
                            <TrendingUp size={18} />
                            체결 모니터링
                        </button>
                    </div>

                    {/* Content */}
                    <div className="p-6 flex-1">
                        {activeTab === 'product' && <AdminProductTab />}
                        {activeTab === 'bid' && <AdminBidTab />}
                        {activeTab === 'trade' && <AdminTradeTab />}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminPage;
