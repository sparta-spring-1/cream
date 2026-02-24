import { Link } from 'react-router-dom';
import { Zap } from 'lucide-react';
import type { PublicSummaryProduct } from '../../api/product';

interface ProductCardProps {
    product: PublicSummaryProduct;
}

export const ProductCard = ({ product }: ProductCardProps) => {
    return (
        <Link
            to={`/products/${product.productId}`}
            className="block group relative bg-white rounded-2xl overflow-hidden border border-gray-100 transition-all hover:shadow-xl hover:-translate-y-1"
        >
            {/* Image Section */}
            <div className="aspect-square bg-gray-50 relative overflow-hidden">
                <div
                    className="absolute inset-0 bg-center bg-cover transition-transform duration-500 group-hover:scale-110"
                    style={{ backgroundImage: `url("https://placehold.co/400x400/f0f0f0/333333?text=Product")` }}
                />
                {/* Badges could go here */}
                <div className="absolute top-3 left-3 bg-white/90 backdrop-blur-sm px-2 py-1 rounded-md text-[10px] font-bold shadow-sm flex items-center gap-1 text-green-600">
                    <Zap size={10} className="fill-current" /> 빠른배송
                </div>
            </div>

            {/* Info Section */}
            <div className="p-4">
                <p className="text-xs font-extrabold text-black mb-1 underline decoration-transparent group-hover:decoration-black transition-all underline-offset-2">
                    {product.brandName}
                </p>
                <p className="text-sm text-gray-600 mb-3 truncate leading-tight min-h-[1.25rem]">
                    {product.name}
                </p>

                <div className="flex items-end justify-between mt-2">
                    <div className="flex flex-col">
                        <span className="text-base font-bold text-gray-900 tracking-tight">
                            {product.retailPrice.toLocaleString()}원
                        </span>
                        <span className="text-[10px] text-gray-400 font-medium">즉시 구매가</span>
                    </div>
                </div>
            </div>
        </Link>
    );
};
