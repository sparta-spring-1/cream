import { Settings, ShoppingBag } from 'lucide-react';
import { useEffect, useState } from 'react';
import { authApi } from '../api/auth';
import { type MeResponse } from '../types/auth';
import MyPaymentHistory from '../components/mypage/MyPaymentHistory';
import MyBidHistory from '../components/mypage/MyBidHistory';

const MyPage = () => {
    const [user, setUser] = useState<MeResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<'buy' | 'sell'>('buy');
    const [buyTab, setBuyTab] = useState<'bid' | 'payment'>('payment'); // 'bid' or 'payment'

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

    return (
        <div className="flex justify-center py-10 min-h-screen bg-white">
            <div className="max-w-[1200px] w-full px-4 flex gap-10">
                {/* Sidebar */}
                <aside className="w-48 hidden md:block">
                    <h2 className="font-bold text-2xl mb-6">마이 페이지</h2>
                    <nav className="flex flex-col gap-4 text-gray-500">
                        <div className="flex flex-col gap-2">
                            <h3 className="font-bold text-black mb-1">쇼핑 정보</h3>
                            <button onClick={() => { setActiveTab('buy'); setBuyTab('payment'); }} className={`text-left hover:text-black ${activeTab === 'buy' ? 'text-black font-bold' : ''}`}>구매 내역</button>
                            <button onClick={() => setActiveTab('sell')} className={`text-left hover:text-black ${activeTab === 'sell' ? 'text-black font-bold' : ''}`}>판매 내역</button>
                            <a href="#" className="hover:text-black">보관 판매</a>
                            <a href="#" className="hover:text-black">관심 상품</a>
                        </div>
                        <div className="flex flex-col gap-2 mt-4">
                            <h3 className="font-bold text-black mb-1">내 정보</h3>
                            <a href="#" className="hover:text-black">로그인 정보</a>
                            <a href="#" className="hover:text-black">프로필 관리</a>
                            <a href="#" className="hover:text-black">주소록</a>
                            <a href="#" className="hover:text-black">결제 정보</a>
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
                            <div className="flex gap-4 mt-4">
                                <button className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50">프로필 수정</button>
                                <button className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50">내 스타일</button>
                            </div>
                        </div>
                    </div>

                    {/* Stats */}
                    <div className="grid grid-cols-3 gap-4 mb-10">
                        {/* Buying */}
                        <div onClick={() => setActiveTab('buy')} className={`p-6 rounded-xl text-center cursor-pointer transition-colors ${activeTab === 'buy' ? 'bg-gray-100' : 'bg-gray-50 hover:bg-gray-100'}`}>
                            <h3 className="text-gray-500 text-sm mb-2">구매 내역</h3>
                            <div className="text-xl font-bold flex flex-col items-center gap-1">
                                -
                                <span className="text-xs font-normal text-gray-400">전체</span>
                            </div>
                        </div>
                        {/* Selling */}
                        <div onClick={() => setActiveTab('sell')} className={`p-6 rounded-xl text-center cursor-pointer transition-colors ${activeTab === 'sell' ? 'bg-gray-100' : 'bg-gray-50 hover:bg-gray-100'}`}>
                            <h3 className="text-gray-500 text-sm mb-2">판매 내역</h3>
                            <div className="text-xl font-bold flex flex-col items-center gap-1">
                                -
                                <span className="text-xs font-normal text-gray-400">전체</span>
                            </div>
                        </div>
                        {/* Wishlist */}
                        <div className="p-6 bg-gray-50 rounded-xl text-center cursor-pointer hover:bg-gray-100 transition-colors">
                            <h3 className="text-gray-500 text-sm mb-2">관심 상품</h3>
                            <div className="text-xl font-bold flex flex-col items-center gap-1">
                                0
                                <span className="text-xs font-normal text-gray-400">개</span>
                            </div>
                        </div>
                    </div>

                    {/* Content Section */}
                    <div>
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="font-bold text-lg">
                                {activeTab === 'buy' ? '구매 내역' : '판매 내역'}
                            </h3>
                            {activeTab === 'buy' && (
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
                                        구매 입찰
                                    </button>
                                </div>
                            )}
                        </div>

                        {activeTab === 'buy' ? (
                            buyTab === 'payment' ? <MyPaymentHistory /> : <MyBidHistory />
                        ) : (
                            // For Sell tab, we reuse BidHistory but filtered?
                            // Currently MyBidHistory shows all. 
                            // Ideally passed a filter prop, but for MVP we show all (with type indicator).
                            // Let's reuse MyBidHistory for now as it shows type (BUY/SELL).
                            <MyBidHistory />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MyPage;
