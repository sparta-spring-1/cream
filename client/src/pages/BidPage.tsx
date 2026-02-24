import { useState, useEffect } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { productApi, type AdminGetOneProductResponse } from '../api/product';
import { bidApi } from '../api/bid';

// Hardcoded mapping for demonstration since backend doesn't return Option IDs yet
const OPTION_ID_MAP: Record<string, number> = {
    '230': 1, '240': 2, '250': 3, '260': 4, '270': 5, '280': 6,
    'S': 7, 'M': 8, 'L': 9, 'XL': 10
};

const BidPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const productId = searchParams.get('productId');
    const isSell = location.pathname.includes('/sell');

    const [product, setProduct] = useState<AdminGetOneProductResponse | null>(null);
    const [selectedSize, setSelectedSize] = useState<string | null>(null);
    const [price, setPrice] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const typeText = isSell ? '판매' : '구매';
    const typeColor = isSell ? 'bg-[#41b979]' : 'bg-[#ef6253]';
    const typeColorHover = isSell ? 'hover:bg-[#38a36a]' : 'hover:bg-[#d95245]';

    useEffect(() => {
        if (productId) {
            productApi.getOne(Number(productId))
                .then(data => setProduct(data))
                .catch(err => {
                    console.error(err);
                    alert("상품 정보를 불러올 수 없습니다.");
                    navigate(-1);
                });
        }
    }, [productId, navigate]);

    const handleAction = async () => {
        if (!selectedSize) {
            alert('사이즈를 선택해주세요.');
            return;
        }
        if (!price) {
            alert('가격을 입력해주세요.');
            return;
        }

        const optionId = OPTION_ID_MAP[selectedSize] || 1; // Fallback to 1

        try {
            setIsLoading(true);
            await bidApi.create({
                productOptionId: optionId,
                price: parseInt(price.replace(/,/g, ''), 10),
                type: isSell ? 'SELL' : 'BUY'
            });

            alert(`${typeText} 입찰이 등록되었습니다.`);
            navigate('/my'); // Go to My Page or Product Page
        } catch (err: any) {
            console.error(err);
            alert(err.response?.data?.message || '입찰 등록에 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    if (!product) return <div className="text-center py-20">Loading...</div>;

    return (
        <div className="flex justify-center bg-gray-50 min-h-screen py-8">
            <div className="max-w-[800px] w-full bg-white p-8 rounded-xl shadow-sm border border-gray-100 flex flex-col gap-8">
                {/* Header */}
                <div className="flex items-center gap-4 border-b border-gray-100 pb-6">
                    <div className="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden flex items-center justify-center">
                        <span className="text-gray-400 text-xs">No Image</span>
                    </div>
                    <div className="flex flex-col">
                        <span className="font-bold text-sm">{product.modelNumber}</span>
                        <span className="text-gray-500 text-xs">{product.name}</span>
                        <span className="font-medium text-sm mt-1">{product.sizeUnit}</span>
                    </div>
                </div>

                {/* Size Selection */}
                <div className="flex flex-col gap-4">
                    <h3 className="font-bold text-lg">사이즈 선택</h3>
                    <div className="grid grid-cols-3 gap-3">
                        {product.options && product.options.map((size) => (
                            <button
                                key={size}
                                onClick={() => setSelectedSize(size)}
                                className={`py-3 rounded-xl border font-medium transition-colors ${selectedSize === size
                                    ? `border-black font-bold ring-1 ring-black`
                                    : 'border-gray-200 hover:border-gray-400'
                                    }`}
                            >
                                {size}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Price Input */}
                <div className="flex flex-col gap-4 border-t border-gray-100 pt-6">
                    <h3 className="font-bold text-lg">{typeText} 희망가</h3>
                    <div className="flex items-center justify-between bg-gray-50 p-4 rounded-xl">
                        <span className="font-bold text-gray-400">가격</span>
                        <input
                            type="number"
                            value={price}
                            onChange={(e) => setPrice(e.target.value)}
                            // placeholder="희망 가격 입력"
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
                        <span className={`text-${isSell ? '[#41b979]' : '[#ef6253]'}`}>
                            {price ? parseInt(price).toLocaleString() : 0}원
                        </span>
                    </div>
                </div>

                {/* Action Button */}
                <button
                    onClick={handleAction}
                    disabled={!selectedSize || !price || isLoading}
                    className={`w-full py-4 rounded-xl text-white font-bold text-lg transition-colors ${typeColor} ${typeColorHover} disabled:bg-gray-300 disabled:cursor-not-allowed`}
                >
                    {isLoading ? '처리 중...' : `${typeText} 입찰하기`}
                </button>
            </div>
        </div>
    );
};

export default BidPage;
