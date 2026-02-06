import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const BidPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const isSell = location.pathname.includes('/sell');

    const [selectedSize, setSelectedSize] = useState<string | null>(null);
    const [price, setPrice] = useState('10,000');

    const typeText = isSell ? '판매' : '구매';
    const typeColor = isSell ? 'bg-[#41b979]' : 'bg-[#ef6253]';
    const typeColorHover = isSell ? 'hover:bg-[#38a36a]' : 'hover:bg-[#d95245]';

    const handleAction = () => {
        if (!selectedSize) {
            alert('사이즈를 선택해주세요.');
            return;
        }
        // Navigate to payment or complete
        if (isSell) {
            alert('판매 입찰이 등록되었습니다. (기능 준비중)');
        } else {
            navigate('/payment');
        }
    };

    return (
        <div className="flex justify-center bg-gray-50 min-h-screen py-8">
            <div className="max-w-[800px] w-full bg-white p-8 rounded-xl shadow-sm border border-gray-100 flex flex-col gap-8">
                {/* Header */}
                <div className="flex items-center gap-4 border-b border-gray-100 pb-6">
                    <div className="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden">
                        <img
                            src="https://placehold.co/200x200/135bec/ffffff?text=TEST"
                            alt="Product"
                            className="w-full h-full object-cover"
                        />
                    </div>
                    <div className="flex flex-col">
                        <span className="font-bold text-sm">Payment Test Item</span>
                        <span className="text-gray-500 text-xs">결제 테스트용 상품</span>
                        <span className="font-medium text-sm mt-1">{isSell ? '9,000원' : '10,000원'}</span>
                    </div>
                </div>

                {/* Size Selection */}
                <div className="flex flex-col gap-4">
                    <h3 className="font-bold text-lg">사이즈 선택</h3>
                    <div className="grid grid-cols-3 gap-3">
                        {['230', '240', '250', '260', '270', '280'].map((size) => (
                            <button
                                key={size}
                                onClick={() => setSelectedSize(size)}
                                className={`py-3 rounded-xl border font-medium transition-colors ${selectedSize === size
                                    ? `border-black font-bold ring-1 ring-black`
                                    : 'border-gray-200 hover:border-gray-400'
                                    }`}
                            >
                                {size}
                                <div className={`text-[10px] items-center  ${isSell ? 'text-[#41b979]' : 'text-[#ef6253]'}`}>
                                    {isSell ? '9,000원' : '10,000원'}
                                </div>
                            </button>
                        ))}
                    </div>
                </div>

                {/* Price Input (Simplified for Demo) */}
                <div className="flex flex-col gap-4 border-t border-gray-100 pt-6">
                    <h3 className="font-bold text-lg">{typeText} 희망가</h3>
                    <div className="flex items-center justify-between bg-gray-50 p-4 rounded-xl">
                        <span className="font-bold text-gray-400">가격</span>
                        <input
                            type="text"
                            value={price}
                            onChange={(e) => setPrice(e.target.value)}
                            className="bg-transparent text-right font-bold text-xl w-full outline-none"
                        />
                        <span className="font-bold ml-1">원</span>
                    </div>
                </div>

                {/* Summary */}
                <div className="flex flex-col gap-2 border-t border-gray-100 pt-6">
                    <div className="flex justify-between items-center text-sm">
                        <span className="text-gray-500">검수비</span>
                        <span>무료</span>
                    </div>
                    <div className="flex justify-between items-center text-sm">
                        <span className="text-gray-500">배송비</span>
                        <span>무료 (이벤트)</span>
                    </div>
                    <div className="flex justify-between items-center font-bold text-lg mt-2">
                        <span>총 결제금액</span>
                        <span className={`text-${isSell ? '[#41b979]' : '[#ef6253]'}`}>{price}원</span>
                    </div>
                </div>

                {/* Action Button */}
                <button
                    onClick={handleAction}
                    disabled={!selectedSize}
                    className={`w-full py-4 rounded-xl text-white font-bold text-lg transition-colors ${typeColor} ${typeColorHover} disabled:bg-gray-300 disabled:cursor-not-allowed`}
                >
                    {isSell ? '판매 입찰하기' : '일반 결제하기'}
                </button>
            </div>
        </div>
    );
};

export default BidPage;
