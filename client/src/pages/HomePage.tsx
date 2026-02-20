import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productApi, type PublicSummaryProduct } from '../api/product';
import { ArrowRight, Zap } from 'lucide-react';

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
                            className="absolute inset-0 bg-center bg-cover transition-transform duration-500 group-hover:scale-105"
                            style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuAg_rBjhCLn6cPLT5FjjclNTFfyN1hQ73p28aFbqQPvd5B0AwPwQsOsH3daB1Peu8RoLPdRtIbi0zaxsQrD15-0C7mdfFTMNK_X9mqA41yitgXpLYDwCpb6IUyEQKSMwYUF-YyNw8J17qIxkZF34T0CThYfOZzp-PELd7bMHVed7gi88ixJ2K9jS3QKM7-47eR5TYRElnpqX-JDlwixgzf5oDmn4y8Mld8Ymid2mBXXizaq_pO1W98aISG-G6OEjsZyUBATwmWiyFU")' }}
                        ></div>
                        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
                        <div className="absolute bottom-10 left-10 text-white">
                            <h3 className="text-3xl font-bold mb-2">Jordan 1 High OG 'Royal Reimagined'</h3>
                            <p className="text-lg opacity-90 mb-6">클래식의 재해석, 프리미엄 스웨이드를 만나보세요.</p>
                            <button className="bg-white text-black px-8 py-3 rounded-lg font-bold hover:bg-gray-200 transition-colors border-none cursor-pointer">구매하기</button>
                        </div>
                    </div>

                    {/* Banner 2 */}
                    <div className="min-w-[400px] h-[360px] relative rounded-xl overflow-hidden group cursor-pointer flex-shrink-0">
                        <div
                            className="absolute inset-0 bg-center bg-cover transition-transform duration-500 group-hover:scale-105"
                            style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuBWko8z3T7FQgdfWL8Eur6qZifUtzqZQLqXS5nimH9wE8mzWGXg_KrmqWkx-pMdQ2-nHhSsCQTIOuOubxFn1mjf9fXO1P3BINAqfBoM2CZYwqEmy_jwFTOWYcTllcU7r_6A6q2mNWRHG0-O1NKipIrXKeiRZ0_X9Ht9oGqjhkzsXfch6OT8d_n6puFwmmfuIe0wRbehC5RN13YI5fxvUiHNfTCpRieE2DSaNbDMkYRImWG49j8H1TUthipjQNabO-iyYsJxiV8XFgI")' }}
                        ></div>
                        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
                        <div className="absolute bottom-10 left-10 text-white">
                            <h3 className="text-2xl font-bold mb-2">Pre-owned Rolex</h3>
                            <p className="text-sm opacity-90 mb-4">검증된 정품 컬렉션</p>
                            <button className="bg-primary text-white px-6 py-2 rounded-lg font-bold hover:bg-primary/90 transition-colors border-none cursor-pointer">둘러보기</button>
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
