import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productApi, type PublicSummaryProduct, type ProductSearchCondition } from '../api/product';
import { ProductCard } from '../components/product/ProductCard';
import { ChevronDown, Filter, X } from 'lucide-react';

const CATEGORIES = ['전체', '신발', '의류', '패션잡화', '라이프', '테크'];
const BRANDS = ['Nike', 'Jordan', 'Adidas', 'New Balance', 'Stussy', 'Asics', 'Supreme'];
const SIZES = ['230', '240', '250', '260', '270', '280', 'S', 'M', 'L', 'XL'];

const ShopPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [products, setProducts] = useState<PublicSummaryProduct[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [hasNext, setHasNext] = useState(false);
    const [total, setTotal] = useState(0);

    // Dynamic Filter states
    const [availableBrands, setAvailableBrands] = useState<string[]>(BRANDS);

    // Initial values from URL search params
    const getInitialCondition = (): ProductSearchCondition => ({
        keyword: searchParams.get('keyword') || undefined,
        category: searchParams.get('category') || undefined,
        brandName: searchParams.get('brandName') || undefined,
        sort: (searchParams.get('sort') as any) || 'RECENT',
        minPrice: searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined,
        maxPrice: searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined,
    });

    const [condition, setCondition] = useState<ProductSearchCondition>(getInitialCondition());

    const fetchProducts = async () => {
        setIsLoading(true);
        try {
            const data = await productApi.getPublicProducts(page, 20, condition);
            setProducts(data.productList);
            setHasNext(data.hasNext);
            setTotal(data.totalElements);

            // Dynamically update unique brands/categories from results if no filters are active
            // This helps discover what's actually in the backend
            if (!condition.brandName && !condition.category && !condition.keyword) {
                const results = data.productList;
                const uniqueBrands = Array.from(new Set(results.map(p => p.brandName)));
                if (uniqueBrands.length > 0) setAvailableBrands(uniqueBrands);

                // Categories are names in PublicSummaryProduct? Let's check DTO.
                // PublicSummaryProduct only has brandName, not category. 
                // Wait, it doesn't have category. Let's stick to Brands for now.
            }
        } catch (err) {
            console.error("Failed to fetch products", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        // When URL params change (e.g., from Header search), update condition
        setCondition(getInitialCondition());
        setPage(0); // Reset to first page on search change
    }, [searchParams]);

    useEffect(() => {
        fetchProducts();
    }, [page, condition]);

    const handleFilterChange = (key: keyof ProductSearchCondition, value: any) => {
        const newCondition = { ...condition, [key]: value };
        if (value === '전체' || value === '') {
            delete (newCondition as any)[key];
        }

        // Update URL to reflect filters
        const newParams = new URLSearchParams(searchParams);
        if (value && value !== '전체') {
            newParams.set(key, value.toString());
        } else {
            newParams.delete(key);
        }
        setSearchParams(newParams);
        setCondition(newCondition);
        setPage(0);
    };

    const clearFilters = () => {
        setSearchParams({});
        setCondition({ sort: 'RECENT' });
        setPage(0);
    };

    return (
        <div className="layout-container py-8 flex gap-8">
            {/* Filter Sidebar */}
            <aside className="w-64 flex-shrink-0 hidden md:block">
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-xl font-bold flex items-center gap-2">
                        <Filter size={20} />
                        Filters
                    </h2>
                    {(Object.keys(condition).length > 1 || (condition.sort && condition.sort !== 'RECENT')) && (
                        <button
                            onClick={clearFilters}
                            className="text-xs text-gray-400 hover:text-black underline"
                        >
                            Reset
                        </button>
                    )}
                </div>

                {/* Categories */}
                <div className="mb-8">
                    <h3 className="font-bold text-sm mb-3">Category</h3>
                    <div className="flex flex-col gap-2">
                        {CATEGORIES.map(cat => (
                            <button
                                key={cat}
                                onClick={() => handleFilterChange('category', cat)}
                                className={`text-left text-sm py-1 transition-colors ${(condition.category === cat || (!condition.category && cat === '전체'))
                                    ? 'font-bold text-black' : 'text-gray-500 hover:text-black'
                                    }`}
                            >
                                {cat}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Brands */}
                <div className="mb-8">
                    <h3 className="font-bold text-sm mb-3">Brand</h3>
                    <div className="flex flex-wrap gap-2">
                        {availableBrands.map(brand => (
                            <button
                                key={brand}
                                onClick={() => handleFilterChange('brandName', condition.brandName === brand ? undefined : brand)}
                                className={`px-3 py-1.5 rounded-full text-xs border transition-colors ${condition.brandName === brand
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'
                                    }`}
                            >
                                {brand}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Sizes */}
                <div className="mb-8">
                    <h3 className="font-bold text-sm mb-3">Size</h3>
                    <div className="flex flex-wrap gap-2">
                        {SIZES.map(size => (
                            <button
                                key={size}
                                onClick={() => handleFilterChange('productSize', condition.productSize === size ? undefined : size)}
                                className={`w-12 h-10 flex items-center justify-center rounded border text-xs transition-colors ${condition.productSize === size
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'
                                    }`}
                            >
                                {size}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Price Range */}
                <div className="mb-8">
                    <h3 className="font-bold text-sm mb-3">Price Range</h3>
                    <div className="flex flex-col gap-3">
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                placeholder="Min"
                                value={condition.minPrice || ''}
                                onChange={(e) => handleFilterChange('minPrice', e.target.value ? Number(e.target.value) : undefined)}
                                className="w-full text-xs p-2 border rounded border-gray-200 bg-gray-50 focus:bg-white outline-none"
                            />
                            <span className="text-gray-400">-</span>
                            <input
                                type="number"
                                placeholder="Max"
                                value={condition.maxPrice || ''}
                                onChange={(e) => handleFilterChange('maxPrice', e.target.value ? Number(e.target.value) : undefined)}
                                className="w-full text-xs p-2 border rounded border-gray-200 bg-gray-50 focus:bg-white outline-none"
                            />
                        </div>
                    </div>
                </div>
            </aside>

            {/* Main Content */}
            <div className="flex-1">
                <div className="flex justify-between items-end mb-6">
                    <div>
                        <h1 className="text-2xl font-bold mb-1">Shop</h1>
                        <p className="text-sm text-gray-400">"{condition.keyword || '전체 상품'}" 검색 결과 ({total.toLocaleString()}개)</p>
                    </div>

                    <div className="flex items-center gap-4">
                        {/* Selected Filters Chips */}
                        <div className="flex gap-2 mr-4">
                            {condition.keyword && (
                                <span className="flex items-center gap-1 px-2 py-1 bg-gray-100 rounded text-xs">
                                    "{condition.keyword}"
                                    <X size={12} className="cursor-pointer" onClick={() => handleFilterChange('keyword', undefined)} />
                                </span>
                            )}
                            {condition.brandName && (
                                <span className="flex items-center gap-1 px-2 py-1 bg-gray-100 rounded text-xs">
                                    {condition.brandName}
                                    <X size={12} className="cursor-pointer" onClick={() => handleFilterChange('brandName', undefined)} />
                                </span>
                            )}
                        </div>

                        {/* Sort Dropdown */}
                        <div className="relative group">
                            <button className="flex items-center gap-1 text-sm font-semibold py-2">
                                {condition.sort === 'RECENT' ? '최신 등록순' : '낮은 가격순'}
                                <ChevronDown size={14} />
                            </button>
                            <div className="absolute right-0 top-full mt-1 bg-white border border-gray-100 shadow-lg rounded-lg py-1 w-32 hidden group-hover:block z-10">
                                <button
                                    onClick={() => handleFilterChange('sort', 'RECENT')}
                                    className={`w-full text-left px-4 py-2 text-xs hover:bg-gray-50 ${condition.sort === 'RECENT' ? 'font-bold' : ''}`}
                                >
                                    최신 등록순
                                </button>
                                <button
                                    onClick={() => handleFilterChange('sort', 'PRICE_ASC')}
                                    className={`w-full text-left px-4 py-2 text-xs hover:bg-gray-50 ${condition.sort === 'PRICE_ASC' ? 'font-bold' : ''}`}
                                >
                                    낮은 가격순
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex flex-col items-center justify-center p-40">
                        <div className="w-10 h-10 border-4 border-gray-200 border-t-black rounded-full animate-spin mb-4" />
                        <p className="text-gray-400 text-sm">Loading products...</p>
                    </div>
                ) : (
                    <>
                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-x-4 gap-y-10">
                            {products.map((product) => (
                                <ProductCard key={product.productId} product={product} />
                            ))}
                        </div>

                        {products.length === 0 && (
                            <div className="text-center py-40 border-2 border-dashed border-gray-100 rounded-3xl">
                                <p className="text-gray-400 font-medium">검색 결과와 일치하는 상품이 없습니다.</p>
                                <button
                                    onClick={clearFilters}
                                    className="mt-4 px-4 py-2 bg-black text-white text-sm rounded-full font-bold"
                                >
                                    전체 보기로 돌아가기
                                </button>
                            </div>
                        )}

                        {/* Pagination */}
                        {hasNext || page > 0 ? (
                            <div className="flex justify-center items-center gap-6 mt-16 pt-8 border-t border-gray-100">
                                <button
                                    disabled={page === 0}
                                    onClick={() => {
                                        setPage(p => Math.max(0, p - 1));
                                        window.scrollTo(0, 0);
                                    }}
                                    className="p-2 border border-gray-200 rounded-full disabled:opacity-30 hover:bg-gray-50 transition-colors"
                                >
                                    <ChevronDown size={20} className="rotate-90" />
                                </button>
                                <span className="font-bold text-sm">Page {page + 1}</span>
                                <button
                                    disabled={!hasNext}
                                    onClick={() => {
                                        setPage(p => p + 1);
                                        window.scrollTo(0, 0);
                                    }}
                                    className="p-2 border border-gray-200 rounded-full disabled:opacity-30 hover:bg-gray-50 transition-colors"
                                >
                                    <ChevronDown size={20} className="-rotate-90" />
                                </button>
                            </div>
                        ) : null}
                    </>
                )}
            </div>
        </div>
    );
};

export default ShopPage;
