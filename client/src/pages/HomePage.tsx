import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productApi, type PublicSummaryProduct } from '../api/product';
import { ArrowRight } from 'lucide-react';
import { ProductCard } from '../components/product/ProductCard';

const HomePage = () => {
    const [products, setProducts] = useState<PublicSummaryProduct[]>([]);

    useEffect(() => {
        productApi.getPublicProducts(0, 5) // Fetch top 5 items
            .then(data => setProducts(data.productList))
            .catch(err => console.error("Failed to fetch products", err));
    }, []);

    return (
        <div className="layout-container py-8 space-y-12">
            {/* ... Hero Section omitted for brevity, keeping it unchanged ... */}
            <section className="max-w-content px-10">
                <div className="flex overflow-x-auto gap-4 hide-scrollbar pb-4 -mx-2 px-2">
                    {/* Banner 1 */}
                    <div className="min-w-[700px] h-[360px] relative rounded-xl overflow-hidden group cursor-pointer flex-shrink-0">
                        <div
                            className="absolute inset-0 bg-gradient-to-br from-[#f8f8f8] to-[#e0e0e0] transition-transform duration-500 group-hover:scale-105"
                        ></div>
                        <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent"></div>
                        <div className="absolute bottom-10 left-10 text-black">
                            <h3 className="text-3xl font-bold mb-2">New Arrivals</h3>
                            <p className="text-lg opacity-90 mb-6">최신 트렌드 상품을 지금 바로 만나보세요.</p>
                            <button className="bg-black text-white px-8 py-3 rounded-lg font-bold hover:bg-gray-800 transition-colors border-none cursor-pointer">구매하기</button>
                        </div>
                    </div>

                    {/* Banner 2 */}
                    <div className="min-w-[400px] h-[360px] relative rounded-xl overflow-hidden group cursor-pointer flex-shrink-0">
                        <div
                            className="absolute inset-0 bg-gradient-to-br from-[#d4af37]/20 to-[#ffd700]/10 transition-transform duration-500 group-hover:scale-105"
                        ></div>
                        <div className="absolute inset-0 bg-gradient-to-t from-black/10 to-transparent"></div>
                        <div className="absolute bottom-10 left-10 text-black">
                            <h3 className="text-2xl font-bold mb-2">Premium Collection</h3>
                            <p className="text-sm opacity-90 mb-4">검증된 프리미엄 에디션</p>
                            <button className="bg-black text-white px-6 py-2 rounded-lg font-bold hover:bg-gray-800 transition-colors border-none cursor-pointer">둘러보기</button>
                        </div>
                    </div>
                </div>
            </section>

            {/* Trending Grid */}
            <section className="max-w-content px-10">
                <div className="flex items-end justify-between mb-6 px-1">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight">인기 상품</h2>
                        <p className="text-gray-500 text-sm mt-1">지금 가장 많이 거래되는 상품</p>
                    </div>
                    <Link to="/products" className="text-primary font-semibold text-sm hover:underline flex items-center gap-1">
                        더 보기 <ArrowRight size={16} />
                    </Link>
                </div>

                <div className="grid grid-cols-5 gap-6">
                    {products.map((product) => (
                        <ProductCard key={product.productId} product={product} />
                    ))}
                </div>
            </section>

            {/* Footer (Simplified) */}
            <footer className="w-full bg-white border-t border-gray-200 mt-20 pb-20">
                <div className="max-w-content mx-auto px-10 py-12">
                    <div className="text-center">
                        <h4 className="font-bold text-xl mb-2">CREAM</h4>
                        <div className="text-sm text-gray-500 space-x-4 mb-8">
                            <a href="#" className="hover:text-primary">회사소개</a>
                            <a href="#" className="hover:text-primary">이용약관</a>
                            <a href="#" className="hover:text-primary">개인정보처리방침</a>
                        </div>
                        <p className="text-xs text-gray-400">© 2026 CREAM Corp.</p>
                    </div>
                </div>
            </footer>
        </div>
    );
};

export default HomePage;
