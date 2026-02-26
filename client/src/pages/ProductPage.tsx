import { Bookmark } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { productApi, type GetOneProductResponse } from '../api/product';
import { bidApi, type BidResponse } from '../api/bid';

const ProductPage = () => {
    const { id } = useParams();
    const isTestItem = false;

    const [product, setProduct] = useState<GetOneProductResponse | null>(null);
    const [isLoading, setIsLoading] = useState(!isTestItem);
    const [selectedOptionId, setSelectedOptionId] = useState<number | null>(null);
    const [marketBids, setMarketBids] = useState<BidResponse[]>([]);


    useEffect(() => {
        if (id) {
            productApi.getPublicProduct(Number(id))
                .then(data => {
                    setProduct(data);
                    // Default to first option if available
                    if (data.options && data.options.length > 0) {
                        setSelectedOptionId(data.options[0].id);
                    }
                })
                .catch(err => {
                    console.error("Failed to fetch product", err);
                    alert("상품 정보를 불러오는데 실패했습니다.");
                })
                .finally(() => setIsLoading(false));
        }
    }, [id, isTestItem]);

    // Fetch market bids when selectedOptionId changes
    useEffect(() => {
        if (selectedOptionId) {
            bidApi.getBidsByProduct(selectedOptionId)
                .then(data => setMarketBids(data))
                .catch(err => console.error("Failed to fetch bids", err));
        }
    }, [selectedOptionId]);

    if (isLoading) return <div className="flex justify-center p-20">Loading...</div>;
    if (!product) return <div className="flex justify-center p-20">Product not found</div>;

    // Helper for display - Handles both {id, size} objects and raw strings
    const renderSize = (option: any) => {
        if (typeof option === 'string') return option;
        return option?.size || '';
    };

    const getOptionId = (option: any) => {
        if (typeof option === 'string') return option; // Should not happen but fallback
        return option?.id;
    };

    const selectedSize = product.options.find(opt => getOptionId(opt) === selectedOptionId)
        ? renderSize(product.options.find(opt => getOptionId(opt) === selectedOptionId))
        : '모든 사이즈';
    const displayPrice = product.retailPrice.toLocaleString() + '원';

    return (
        <div className="flex justify-center py-8">
            <div className="max-w-[1200px] w-full px-4 flex flex-col gap-8">
                {/* Breadcrumbs */}
                <div className="flex flex-wrap items-center gap-2">
                    <a className="text-gray-500 text-sm font-medium" href="#">{product.brandName}</a>
                    <span className="text-gray-300">/</span>
                    <span className="text-black font-bold text-sm">{product.name}</span>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
                    {/* Left: Image Gallery */}
                    <div className="flex flex-col gap-4">
                        <div className="aspect-square w-full rounded-xl bg-gray-50 flex items-center justify-center border border-gray-100">
                            {product.imageIds && product.imageIds.length > 0 ? (
                                <img className="w-full h-full object-contain p-8" alt={product.name} src={product.imageIds[0]} />
                            ) : (
                                <span className="text-gray-400">이미지 없음</span>
                            )}
                        </div>
                        {/* Thumbnail gallery omitted as we don't have images */}
                    </div>

                    {/* Right: Product Details */}
                    <div className="flex flex-col">
                        <div className="border-b border-gray-200 pb-6">
                            <h1 className="text-3xl font-bold text-black">{product.name}</h1>
                            <p className="text-xl text-gray-500 mt-1">{product.modelNumber}</p>
                            <p className="text-gray-400 text-sm mt-2">{product.color}</p>
                        </div>

                        <div className="grid grid-cols-2 gap-4 py-6 border-b border-gray-200">
                            <div className="flex flex-col">
                                <span className="text-xs text-gray-400 uppercase font-bold tracking-wider">스타일 코드</span>
                                <span className="text-base font-medium text-black">{product.modelNumber}</span>
                            </div>
                            <div className="flex flex-col">
                                <span className="text-xs text-gray-400 uppercase font-bold tracking-wider">발매일</span>
                                <span className="text-base font-medium text-black">{product.retailDate?.substring(0, 10)}</span>
                            </div>
                            <div className="flex flex-col mt-4">
                                <span className="text-xs text-gray-400 uppercase font-bold tracking-wider">컬러</span>
                                <span className="text-base font-medium text-black">{product.color}</span>
                            </div>
                            <div className="flex flex-col mt-4">
                                <span className="text-xs text-gray-400 uppercase font-bold tracking-wider">발매가</span>
                                <span className="text-base font-medium text-black">{displayPrice}</span>
                            </div>
                        </div>

                        {/* Actions */}
                        <div className="py-8 flex flex-col gap-4">
                            <div className="flex flex-col gap-3">
                                <div className="flex items-center justify-between">
                                    <span className="text-sm font-bold text-black">사이즈 선택</span>
                                    <span className="text-sm font-medium text-gray-500">{selectedSize}</span>
                                </div>
                                <div className="grid grid-cols-4 gap-2">
                                    {product.options.map((option: any) => (
                                        <button
                                            key={getOptionId(option)}
                                            onClick={() => setSelectedOptionId(getOptionId(option))}
                                            className={`py-2 text-xs rounded-lg border transition-all ${selectedOptionId === getOptionId(option)
                                                ? 'border-black font-bold bg-gray-50'
                                                : 'border-gray-200 text-gray-400 hover:border-gray-400'
                                                }`}
                                        >
                                            {renderSize(option)}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="flex gap-4 mt-4">
                                <Link
                                    to={`/bids/buy?productId=${product.id}${selectedOptionId ? `&optionId=${selectedOptionId}` : ''}`}
                                    className="flex-1 bg-[#ef6253] hover:bg-[#d95245] text-white rounded-xl h-14 flex items-center no-underline"
                                >
                                    <div className="w-1/3 border-r border-white/20 h-full flex items-center justify-center font-bold">구매</div>
                                    <div className="w-2/3 px-4 flex flex-col items-start">
                                        <span className="text-lg font-bold">{displayPrice}</span>
                                        <span className="text-[10px] opacity-80">구매 입찰가</span>
                                    </div>
                                </Link>
                                <Link
                                    to={`/bids/sell?productId=${product.id}${selectedOptionId ? `&optionId=${selectedOptionId}` : ''}`}
                                    className="flex-1 bg-[#41b979] hover:bg-[#38a36a] text-white rounded-xl h-14 flex items-center no-underline"
                                >
                                    <div className="w-1/3 border-r border-white/20 h-full flex items-center justify-center font-bold">판매</div>
                                    <div className="w-2/3 px-4 flex flex-col items-start">
                                        <span className="text-lg font-bold">{displayPrice}</span>
                                        <span className="text-[10px] opacity-80">판매 입찰가</span>
                                    </div>
                                </Link>
                            </div>

                            <button className="w-full border border-gray-200 rounded-xl py-4 flex items-center justify-center gap-2 hover:bg-gray-50 transition-colors">
                                <Bookmark className="text-gray-400" size={20} />
                                <span className="text-black font-bold">관심상품</span>
                            </button>
                        </div>

                        {/* Market Price Section */}
                        <div className="mt-8">
                            <h3 className="font-bold text-lg mb-1">체결 거래 (최근 시세)</h3>
                            <p className="text-xs text-gray-400 mb-4">{selectedSize} 기준</p>
                            <div className="border-t border-gray-100">
                                <div className="flex text-xs text-gray-400 py-2 border-b border-gray-100">
                                    <span className="flex-1">가격</span>
                                    <span className="flex-1 text-center">상태</span>
                                    <span className="flex-1 text-right">거래일</span>
                                </div>
                                {marketBids.length > 0 ? (
                                    marketBids.slice(0, 5).map(bid => (
                                        <div key={bid.id} className="flex text-sm py-2 border-b border-gray-50">
                                            <span className="flex-1 font-bold">{bid.price.toLocaleString()}원</span>
                                            <span className="flex-1 text-center">
                                                <span className={`px-2 py-0.5 rounded-full text-[10px] ${bid.type === 'BUY' ? 'text-[#ef6253] bg-[#ef6253]/10' : 'text-[#41b979] bg-[#41b979]/10'}`}>
                                                    {bid.type === 'BUY' ? '구매희망' : '판매희망'}
                                                </span>
                                            </span>
                                            <span className="flex-1 text-right text-gray-400">{bid.createdAt ? bid.createdAt.substring(0, 10) : '-'}</span>
                                        </div>
                                    ))
                                ) : (
                                    <div className="py-8 text-center text-gray-400 text-sm">해당 사이즈의 입찰 내역이 없습니다.</div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductPage;
