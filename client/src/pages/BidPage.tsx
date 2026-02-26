import { useState, useEffect } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { productApi, type GetOneProductResponse } from '../api/product';
import { bidApi, type BidRequest } from '../api/bid'; //

const BidPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const productId = searchParams.get('productId');
    const preSelectedOptionId = searchParams.get('optionId');
    const bidId = searchParams.get('bidId');

    // 1. 타입 선택 상태 추가 (기본값은 URL 경로 기준)
    const [bidType, setBidType] = useState<'BUY' | 'SELL'>(
        location.pathname.includes('/sell') ? 'SELL' : 'BUY'
    );

    const [product, setProduct] = useState<GetOneProductResponse | null>(null);
    const [selectedOptionId, setSelectedOptionId] = useState<number | string | null>(
        preSelectedOptionId ? (isNaN(Number(preSelectedOptionId)) ? preSelectedOptionId : Number(preSelectedOptionId)) : null
    );
    const [price, setPrice] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    // 디자인 설정
    const isSell = bidType === 'SELL';
    const typeText = isSell ? '판매' : '구매';
    const activeColor = isSell ? 'bg-[#41b979]' : 'bg-[#ef6253]';

    useEffect(() => {
        if (productId) {
            productApi.getPublicProduct(Number(productId))
                .then(data => setProduct(data))
                .catch(() => {
                    alert("상품 정보를 불러올 수 없습니다.");
                    navigate(-1);
                });
        }
    }, [productId]);

    const handleAction = async () => {
        if (!selectedOptionId || !price) {
            alert('필수 정보를 입력해주세요.');
            return;
        }

        try {
            setIsLoading(true);
            const priceValue = Math.floor(Number(price.toString().replace(/,/g, '')));

            // 백엔드 BidRequestDto 형식에 맞춤
            const requestData: BidRequest = {
                productOptionId: Number(selectedOptionId),
                price: priceValue,
                type: bidType // 사용자가 선택한 타입 전송
            };

            if (bidId && bidId !== 'null') {
                await bidApi.updateBid(Number(bidId), requestData); //
                alert('입찰 정보가 수정되었습니다.');
            } else {
                await bidApi.create(requestData); //
                alert(`${typeText} 입찰이 등록되었습니다.`);
            }
            navigate('/me');
        } catch (err: any) {
            alert(err.response?.data?.message || '입찰 처리 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    if (!product) return <div className="text-center py-20">Loading...</div>;

    return (
        <div className="flex justify-center bg-gray-50 min-h-screen py-8">
            <div className="max-w-[800px] w-full bg-white p-8 rounded-xl shadow-sm border border-gray-100 flex flex-col gap-8">

                {/* 2. 입찰 타입 선택 UI (구매/판매 전환) */}
                <div className="flex flex-col gap-4">
                    <h3 className="font-bold text-lg">입찰 유형 선택</h3>
                    <div className="flex bg-gray-100 p-1 rounded-xl">
                        <button
                            onClick={() => setBidType('BUY')}
                            className={`flex-1 py-3 rounded-lg font-bold transition-all ${
                                bidType === 'BUY' ? 'bg-[#ef6253] text-white shadow-md' : 'text-gray-500'
                            }`}
                        >
                            구매 입찰
                        </button>
                        <button
                            onClick={() => setBidType('SELL')}
                            className={`flex-1 py-3 rounded-lg font-bold transition-all ${
                                bidType === 'SELL' ? 'bg-[#41b979] text-white shadow-md' : 'text-gray-500'
                            }`}
                        >
                            판매 입찰
                        </button>
                    </div>
                </div>

                {/* Header & Size Selection (기존 동일) */}
                <div className="border-b border-gray-100 pb-6 flex items-center gap-4">
                    <div className="flex flex-col">
                        <span className="font-bold text-sm">{product.modelNumber}</span>
                        <span className="text-gray-500 text-xs">{product.name}</span>
                    </div>
                </div>

                <div className="flex flex-col gap-4">
                    <h3 className="font-bold text-lg">사이즈 선택</h3>
                    <div className="grid grid-cols-4 gap-2">
                        {product.options.map((option: any) => (
                            <button
                                key={option.id}
                                onClick={() => setSelectedOptionId(option.id)}
                                className={`py-2 text-sm border rounded-lg ${
                                    option.id === selectedOptionId ? 'border-black bg-gray-50 font-bold' : 'border-gray-200 text-gray-500'
                                }`}
                            >
                                {option.size}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Price Input */}
                <div className="flex flex-col gap-4 pt-6 border-t">
                    <h3 className="font-bold text-lg">{typeText} 희망가</h3>
                    <div className="flex items-center justify-between bg-gray-50 p-4 rounded-xl border-2 border-transparent focus-within:border-gray-200">
                        <span className="font-bold text-gray-400">가격</span>
                        <input
                            type="text"
                            value={price}
                            onChange={(e) => setPrice(e.target.value.replace(/[^0-9]/g, ''))}
                            placeholder="0"
                            className="bg-transparent text-right font-bold text-xl w-full outline-none"
                        />
                        <span className="font-bold ml-1">원</span>
                    </div>
                </div>

                {/* Action Button */}
                <button
                    onClick={handleAction}
                    disabled={!selectedOptionId || !price || isLoading}
                    className={`w-full py-4 rounded-xl text-white font-bold text-lg transition-colors ${activeColor} disabled:bg-gray-300`}
                >
                    {isLoading ? '처리 중...' : bidId ? `${typeText} 수정 완료` : `${typeText} 입찰하기`}
                </button>
            </div>
        </div>
    );
};

export default BidPage;
