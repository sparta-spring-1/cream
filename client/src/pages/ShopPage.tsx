import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productApi, type PublicSummaryProduct } from '../api/product';
import { ProductCard } from '../components/product/ProductCard';

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
                        <ProductCard key={product.productId} product={product} />
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
