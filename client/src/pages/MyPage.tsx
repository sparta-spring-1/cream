import { ShoppingBag } from 'lucide-react';
import { useEffect, useState } from 'react';
import { authApi } from '../api/auth';
import { type MeResponse } from '../types/auth';
import MyPaymentHistory from '../components/mypage/MyPaymentHistory';
import MyBidHistory from '../components/mypage/MyBidHistory';
import MySettlementHistory from '../components/mypage/MySettlementHistory';

const MyPage = () => {
    const [user, setUser] = useState<MeResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<'overview' | 'buying' | 'selling' | 'settlement' | 'wishlist'>('overview');
    const [buyTab, setBuyTab] = useState<'bid' | 'payment'>('payment');

    useEffect(() => {
        authApi.me()
            .then(response => setUser(response.data))
            .catch(err => {
                console.error("Failed to fetch user info", err);
            })
            .finally(() => setIsLoading(false));
    }, []);

    if (isLoading) return <div className="flex justify-center p-20">Loading...</div>;
    if (!user) return <div className="flex justify-center p-20">로그인이 필요합니다.</div>;

    const renderContent = () => {
        switch (activeTab) {
            case 'settlement':
                return <MySettlementHistory />;
            case 'buying':
                return (
                    <div>
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="font-bold text-lg">결제 내역</h3>
                            <div className="flex bg-gray-100 p-1 rounded-lg">
                                <button
                                    onClick={() => setBuyTab('payment')}
                                    className={`px-3 py-1 text-sm rounded-md ${buyTab === 'payment' ? 'bg-white shadow-sm font-bold' : 'text-gray-500'}`}
                                >
                                    결제 완료
                                </button>
                                <button
                                    onClick={() => setBuyTab('bid')}
                                    className={`px-3 py-1 text-sm rounded-md ${buyTab === 'bid' ? 'bg-white shadow-sm font-bold' : 'text-gray-500'}`}
                                >
                                    전체 내역
                                </button>
                            </div>
                        </div>
                        {buyTab === 'payment' ? <MyPaymentHistory onlyPaid={true} /> : <MyPaymentHistory />}
                    </div>
                );
            case 'selling':
                return (
                    <div>
                        <h3 className="font-bold text-lg mb-4">입찰 내역</h3>
                        <MyBidHistory />
                    </div>
                );

            case 'overview':
            default:
                return (
                    <>
                        {/* Stats */}
                        <div className="grid grid-cols-3 gap-4 mb-10">
                            {/* Buying */}
                            <div onClick={() => setActiveTab('buying')} className="p-6 bg-gray-50 rounded-xl text-center cursor-pointer hover:bg-gray-100 transition-colors">
                                <h3 className="text-gray-500 text-sm mb-2">결제 내역</h3>
                                <div className="text-xl font-bold flex flex-col items-center gap-1">
                                    -
                                    <span className="text-xs font-normal text-gray-400">전체</span>
                                </div>
                            </div>
                            {/* Selling */}
                            <div onClick={() => setActiveTab('selling')} className="p-6 bg-gray-50 rounded-xl text-center cursor-pointer hover:bg-gray-100 transition-colors">
                                <h3 className="text-gray-500 text-sm mb-2">입찰 내역</h3>
                                <div className="text-xl font-bold flex flex-col items-center gap-1">
                                    -
                                    <span className="text-xs font-normal text-gray-400">전체</span>
                                </div>
                            </div>
                        </div>

                        {/* Recent History Placeholder */}
                        <div>
                            <h3 className="font-bold text-lg mb-4">최근 거래 내역</h3>
                            <div className="py-20 flex flex-col items-center justify-center border-t border-b border-gray-100">
                                <ShoppingBag className="text-gray-300 mb-4" size={48} />
                                <p className="text-gray-500">거래 내역이 없습니다.</p>
                            </div>
                        </div>
                    </>
                );
        }
    };

    return (
        <div className="flex justify-center py-10 min-h-screen bg-white">
            <div className="max-w-[1200px] w-full px-4 flex gap-10">
                {/* Sidebar */}
                <aside className="w-48 hidden md:block">
                    <h2 className="font-bold text-2xl mb-6 cursor-pointer" onClick={() => setActiveTab('overview')}>마이 페이지</h2>
                    <nav className="flex flex-col gap-4 text-gray-500">
                        {user.role === 'ADMIN' && (
                            <button
                                onClick={() => window.location.href = '/admin'}
                                className="text-left text-red-500 font-bold hover:text-red-600 mb-2"
                            >
                                관리자 대시보드
                            </button>
                        )}
                        <div className="flex flex-col gap-2">
                            <h3 className="font-bold text-black mb-1">쇼핑 정보</h3>
                            <button onClick={() => setActiveTab('buying')} className={`text-left hover:text-black ${activeTab === 'buying' ? 'text-black font-bold' : ''}`}>결제 내역</button>
                            <button onClick={() => setActiveTab('selling')} className={`text-left hover:text-black ${activeTab === 'selling' ? 'text-black font-bold' : ''}`}>입찰 내역</button>
                            <button onClick={() => setActiveTab('settlement')} className={`text-left hover:text-black ${activeTab === 'settlement' ? 'text-black font-bold' : ''}`}>정산 내역</button>

                        </div>
                    </nav>
                </aside>

                {/* Main Content */}
                <div className="flex-1">
                    {/* User Profile Card */}
                    <div className="flex items-center gap-6 p-6 border border-gray-200 rounded-xl mb-8">
                        <div className="w-24 h-24 rounded-full bg-gray-200 overflow-hidden">
                            <img src={`https://ui-avatars.com/api/?name=${user.name}&background=random`} alt="Profile" className="w-full h-full object-cover" />
                        </div>
                        <div className="flex-1">
                            <h2 className="text-xl font-bold">{user.name}</h2>
                            <p className="text-gray-400 text-sm">{user.email}</p>

                        </div>
                    </div>
                    {renderContent()}
                </div>
            </div>
        </div>
    );
};

export default MyPage;
