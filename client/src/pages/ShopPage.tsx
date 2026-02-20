import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { productApi, type PublicSummaryProduct } from '../api/product';
import { Zap } from 'lucide-react';

const ShopPage = () => {
    const [searchParams] = useSearchParams();
    const [products, setProducts] = useState<PublicSummaryProduct[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [hasNext, setHasNext] = useState(false);

    useEffect(() => {
        const fetchProducts = async () => {
            setIsLoading(true);
            try {
                const keyword = searchParams.get('keyword') || undefined;
                const data = await productApi.getPublicProducts(page, 20, keyword);
                setProducts(data.productList);
                setHasNext(data.hasNext);
            } catch (err) {
                console.error("Failed to fetch products", err);
            } finally {
                setIsLoading(false);
            }
        };

        fetchProducts();
    }, [page, searchParams]);

    return (
        <div className="layout-container py-8">
            <h1 className="text-2xl font-bold mb-6 px-10">Shop</h1>

            {isLoading ? (
                <div className="flex justify-center p-20">Loading...</div>
            ) : (
                <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-6 px-10">
                    {/* Placeholder for Payment Test Item if needed, or just list API products */}

                    {products.map((product) => (
                        <Link key={product.productId} to={`/products/${product.productId}`} className="group cursor-pointer">
                            <div className="relative aspect-square rounded-xl bg-gray-100 overflow-hidden mb-4">
                                <div
                                    className="absolute inset-0 bg-center bg-cover group-hover:scale-110 transition-transform duration-500"
                                    style={{ backgroundImage: `url("https://placehold.co/400x400/f0f0f0/333333?text=No+Image")` }}
                                />
                            </div>
                            <div>
                                <p className="font-bold text-sm text-black">{product.brandName}</p>
                                <p className="text-gray-500 text-xs truncate mb-2">{product.name}</p>
                                <div className="flex flex-col">
                                    <span className="text-sm font-black text-black">{product.retailPrice.toLocaleString()}원</span>
                                    <span className="text-[10px] text-gray-400 font-medium uppercase tracking-wider flex items-center gap-1">
                                        <Zap size={10} className="fill-current" /> 빠른배송
                                    </span>
                                </div>
                            </div>
                        </Link>
                    ))}

                    {products.length === 0 && (
                        <div className="col-span-full text-center py-20 text-gray-500">
                            등록된 상품이 없습니다.
                        </div>
                    )}
                </div>
            )}

            {/* Simple Pagination Controls (can be improved) */}
            <div className="flex justify-center gap-4 mt-8">
                <button
                    disabled={page === 0}
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    className="px-4 py-2 border rounded disabled:opacity-50"
                >
                    Prev
                </button>
                <span className="py-2">Page {page + 1}</span>
                <button
                    disabled={!hasNext}
                    onClick={() => setPage(p => p + 1)}
                    className="px-4 py-2 border rounded disabled:opacity-50"
                >
                    Next
                </button>
            </div>
        </div>
    );
};

export default ShopPage;
