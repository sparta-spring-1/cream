import { ArrowRight, Zap, CheckCircle } from 'lucide-react';
import { Link } from 'react-router-dom';

const HomePage = () => {

    return (
        <div className="layout-container py-8 space-y-12">
            {/* Hero Section */}
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
                    {/* Payment Test Item */}
                    <Link to="/products/payment-test" className="group cursor-pointer">
                        <div className="relative aspect-square rounded-xl bg-gray-100 overflow-hidden mb-4 border-2 border-primary/20">
                            <div
                                className="absolute inset-0 bg-center bg-cover group-hover:scale-110 transition-transform duration-500"
                                style={{ backgroundImage: 'url("https://placehold.co/400x400/135bec/ffffff?text=TEST")' }}
                            />
                            <div className="absolute top-2 left-2 bg-primary text-white text-[10px] font-bold px-2 py-1 rounded">
                                TEST
                            </div>
                        </div>
                        <div>
                            <p className="font-bold text-sm">Payment Test Item</p>
                            <p className="text-gray-500 text-xs truncate mb-2">결제 테스트용 상품</p>
                            <div className="flex flex-col">
                                <span className="text-sm font-black">10,000원</span>
                                <span className="text-[10px] text-primary font-bold uppercase tracking-wider flex items-center gap-1">
                                    <Zap size={10} className="fill-current" /> 테스트
                                </span>
                            </div>
                        </div>
                    </Link>

                    {/* Card 1 */}
                    <div className="group cursor-pointer">
                        <div className="relative aspect-square rounded-xl bg-gray-100 overflow-hidden mb-4">
                            <div
                                className="absolute inset-0 bg-center bg-cover group-hover:scale-110 transition-transform duration-500"
                                style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuCfxhrl7NrltfqgQ2ZlR3OwRy7xy3IxiVpFTMxXhm5pKwISzB0tsr1PcT2f44HBrtbuhFK_TVY3wX8lyiE_JFOD5_5ueMVOVML1f4N-agFGSyk0lohakbdkjGGLmje0PLFtbFFMtY0GNc11SU1BC1XZHlFptdWz1IDxekIKFw4YK6_pNjpaEyjPGGBoeIR7PzeVVIIsnfL_ilOMCuJ3tyDAL7N26_Psj9v98SsGED3_jH55rd5HFThmF10nWiQX2nd1aRhpmIwh4is")' }}
                            />
                        </div>
                        <div>
                            <p className="font-bold text-sm">Nike x Travis Scott</p>
                            <p className="text-gray-500 text-xs truncate mb-2">Air Jordan 1 Low 'Olive'</p>
                            <div className="flex flex-col">
                                <span className="text-sm font-black">645,000원</span>
                                <span className="text-[10px] text-gray-400 font-medium uppercase tracking-wider flex items-center gap-1">
                                    <Zap size={10} className="fill-current" /> 빠른배송
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Card 2 */}
                    <div className="group cursor-pointer">
                        <div className="relative aspect-square rounded-xl bg-gray-100 overflow-hidden mb-4">
                            <div
                                className="absolute inset-0 bg-center bg-cover group-hover:scale-110 transition-transform duration-500"
                                style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuDAh0RgJI2n0LHq9lfy2FL4lE1S6fdW4pjNhmXo7jn7x4X7Dtz_TF811VxK6uzkn3fdMEY2lk-8wp5pYeSH6u4ZJXCJS4d5LZj1loY_Gz_5hMON810997oNFjZJy4sp0DfQVM4FNMQnT_UtRIa5b6Iw2RWKv4DX4ZNE6CosdsPeRnqCYB4Z53S9tBeCpblKJoY7CZJIvlF2qSbsrhbmtrzC6u30-81tWWSHCjwN21-5rRT2_dTun3HHDPOQW51zEyfyASinoxcM0So")' }}
                            />
                        </div>
                        <div>
                            <p className="font-bold text-sm">Adidas Samba OG</p>
                            <p className="text-gray-500 text-xs truncate mb-2">Cloud White Core Black</p>
                            <div className="flex flex-col">
                                <span className="text-sm font-black">139,000원</span>
                                <span className="text-[10px] text-gray-400 font-medium uppercase tracking-wider">빠른구매</span>
                            </div>
                        </div>
                    </div>

                    {/* Card 3 */}
                    <div className="group cursor-pointer">
                        <div className="relative aspect-square rounded-xl bg-gray-100 overflow-hidden mb-4">
                            <div
                                className="absolute inset-0 bg-center bg-cover group-hover:scale-110 transition-transform duration-500"
                                style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuCIq96uNuWMj0SotQyxcfEGWkr9AwMyn7nUnMnkL4K_e_WsHSJkSy3Cd9us3w44YUOVjwyxQ2dJec2sRtYDJs5LZf4Tk-GMSFOqkxRxrNKyt6g2oOkvf6-9WdQBy1U-uMGWUfSjwj9fucehcG__bWphCwY5AI8W2e5LHyh3EkS2mXfs2VOaHRZxeWwRXdcLOqG8yh250seM1kttO0nfJE46haJm8puSELy9BkaGRkkl3PKRnFC6UN_iwDrttkk1uiQuTfG2btQYakA")' }}
                            />
                        </div>
                        <div>
                            <p className="font-bold text-sm">Stussy 8 Ball Tee</p>
                            <p className="text-gray-500 text-xs truncate mb-2">Black (2024 Collection)</p>
                            <div className="flex flex-col">
                                <span className="text-sm font-black">82,000원</span>
                                <span className="text-[10px] text-gray-400 font-medium uppercase tracking-wider">입찰대기</span>
                            </div>
                        </div>
                    </div>

                    {/* Card 4 */}
                    <div className="group cursor-pointer">
                        <div className="relative aspect-square rounded-xl bg-gray-100 overflow-hidden mb-4">
                            <div
                                className="absolute inset-0 bg-center bg-cover group-hover:scale-110 transition-transform duration-500"
                                style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuBj5dyVMpOWegUebtXEu6Stw_PpvDqGV-9qHrUxTJCohWYysJchCfOtZHwcED_swIEwaZwPwQGRUmaHer41VQBm6tN2uy34m3SXa9URJiUcpKW34ADKBaZyMrqvfdubanbEz-_yFnzQX8od7pu_6vW5mmIebCEO9K5g8X7Ympv6BQee2Iq0okzXYbIlNf9qKnof38IVsze98BuYqsXsfl0bIwmQjEVt3dmusajNkUOydfYuK9X1PsApqpTxyXz-IH_A9MRuA0obxQk")' }}
                            />
                        </div>
                        <div>
                            <p className="font-bold text-sm">Prada Re-Edition</p>
                            <p className="text-gray-500 text-xs truncate mb-2">2005 Nylon Shoulder Bag</p>
                            <div className="flex flex-col">
                                <span className="text-sm font-black">1,650,000원</span>
                                <span className="text-[10px] text-primary font-bold uppercase tracking-wider flex items-center gap-1">
                                    <CheckCircle size={10} /> 정품보증
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Card 5 */}
                    <div className="group cursor-pointer">
                        <div className="relative aspect-square rounded-xl bg-gray-100 overflow-hidden mb-4">
                            <div
                                className="absolute inset-0 bg-center bg-cover group-hover:scale-110 transition-transform duration-500"
                                style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuDfWc0Y9bAn4mNvQa0VnyEXlOEFaH2Rs1E8d6hghKIEK0M81i8cb4T1NXvFCZoDEuZd6fHjHEG1MidaASzRTLsYzmxPLunOg1bkpwxv7s5R-N3bkhu5tT8mCVUWDJeisQUH97OVKYI1DojYOn_K_82mMa6yDIgEl6oMzBbAQfj3olegWxm2MHln7TcGzVJv7QPQrZ3JE0tJAAFUhBOJIVuKQo9UIhV1RnXoU-lESqUkeA1eDV0ByH6BVxRiJZqKqBSuwiz5Jo7jDt0")' }}
                            />
                        </div>
                        <div>
                            <p className="font-bold text-sm">New Balance 2002R</p>
                            <p className="text-gray-500 text-xs truncate mb-2">Protection Pack Rain Cloud</p>
                            <div className="flex flex-col">
                                <span className="text-sm font-black">289,000원</span>
                                <span className="text-[10px] text-gray-400 font-medium uppercase tracking-wider">거래 2.4만</span>
                            </div>
                        </div>
                    </div>
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
