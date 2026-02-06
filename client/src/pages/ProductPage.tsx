import { ChevronDown, Bookmark } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { productApi, type AdminGetOneProductResponse } from '../api/product';
import { bidApi, type BidResponse } from '../api/bid';

const ProductPage = () => {
    const { id } = useParams();
    const isTestItem = id === 'payment-test';

    const [product, setProduct] = useState<AdminGetOneProductResponse | null>(null);
    const [isLoading, setIsLoading] = useState(!isTestItem);
    const [marketBids, setMarketBids] = useState<BidResponse[]>([]);

    useEffect(() => {
        if (isTestItem) {
            setProduct({
                id: 1,
                name: "Payment Test Item",
                brandName: "Cream Test",
                modelNumber: "TEST-10000",
                retailPrice: 10000,
                // Dummy data filling
                categoryId: 0,
                imageIds: [],
                options: ['230', '240', '250', '260', '270', '280'],
                color: 'Black/White',
                sizeUnit: 'mm',
                productStatus: 'ON_SALE',
                operationStatus: 'NORMAL',
                retailDate: new Date().toISOString(),
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
            });
            setIsLoading(false);
            return;
        }

        if (id) {
            productApi.getOne(Number(id))
                .then(data => {
                    setProduct(data);
                })
                .catch(err => {
                    console.error("Failed to fetch product", err);
                    alert("상품 정보를 불러오는데 실패했습니다.");
                })
                .finally(() => setIsLoading(false));

            // Fetch Market Price (Hardcoded option ID 1 as requested)
            bidApi.getBidsByProduct(1)
                .then(data => setMarketBids(data))
                .catch(err => console.error("Failed to fetch bids", err));
        }
    }, [id, isTestItem]);

    if (isLoading) return <div className="flex justify-center p-20">Loading...</div>;
    if (!product) return <div className="flex justify-center p-20">Product not found</div>;

    // Helper for display
    const displayPrice = product.retailPrice.toLocaleString() + '원';
    // Since we don't have URLs, use a placeholder
    const imageUrl = "https://placehold.co/600x600/f0f0f0/333333?text=No+Image";

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
                            <img className="w-full h-full object-contain p-8" alt={product.name} src={imageUrl} />
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
                            <div className="flex items-center justify-between">
                                <span className="text-sm font-bold text-black">모든 사이즈</span>
                                <button className="text-sm font-medium text-black flex items-center gap-1">
                                    Size <ChevronDown size={16} />
                                </button>
                            </div>

                            <div className="flex gap-4">
                                <Link to="/bids/buy" className="flex-1 bg-[#ef6253] hover:bg-[#d95245] text-white rounded-xl h-14 flex items-center no-underline">
                                    <div className="w-1/3 border-r border-white/20 h-full flex items-center justify-center font-bold">구매</div>
                                    <div className="w-2/3 px-4 flex flex-col items-start">
                                        <span className="text-lg font-bold">{displayPrice}</span>
                                        <span className="text-[10px] opacity-80">구매 입찰가</span>
                                    </div>
                                </Link>
                                <Link to="/bids/sell" className="flex-1 bg-[#41b979] hover:bg-[#38a36a] text-white rounded-xl h-14 flex items-center no-underline">
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
                            <h3 className="font-bold text-lg mb-4">체결 거래 (최근 시세)</h3>
                            <div className="border-t border-gray-100">
                                <div className="flex text-xs text-gray-400 py-2 border-b border-gray-100">
                                    <span className="flex-1">가격</span>
                                    <span className="flex-1 text-center">상태</span>
                                    <span className="flex-1 text-right">거래일</span>
                                    {/* Actually bid date, as we are showing Bids, not Trades */}
                                </div>
                                {marketBids.length > 0 ? (
                                    marketBids.slice(0, 5).map(bid => (
                                        <div key={bid.id} className="flex text-sm py-2 border-b border-gray-50">
                                            <span className="flex-1 font-bold">{bid.price.toLocaleString()}원</span>
                                            <span className="flex-1 text-center text-gray-500">{bid.status}</span>
                                            <span className="flex-1 text-right text-gray-400">-</span>
                                        </div>
                                    ))
                                ) : (
                                    <div className="py-8 text-center text-gray-400 text-sm">입찰 내역이 없습니다.</div>
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
