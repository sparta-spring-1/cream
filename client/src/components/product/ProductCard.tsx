import { Link } from 'react-router-dom';
import { type PublicSummaryProduct } from '../../api/product';

interface ProductCardProps {
    product: PublicSummaryProduct;
}

export const ProductCard = ({ product }: ProductCardProps) => {
    // Backend doesn't provide image URLs in the summary yet, so we use a clean CSS placeholder
    return (
        <Link to={`/products/${product.productId}`} className="group flex flex-col gap-3 no-underline">
            <div className="aspect-square w-full rounded-xl bg-gray-50 flex items-center justify-center border border-gray-100 overflow-hidden relative">
                {/* Visual placeholder instead of dummy image URL */}
                <div className="absolute inset-0 bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center">
                    <span className="text-gray-400 font-bold text-lg select-none">{product.brandName}</span>
                </div>

                {/* If we had a real image, it would go here */}
                {/* <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" /> */}
            </div>

            <div className="flex flex-col gap-1 px-1">
                <span className="text-sm font-bold text-black border-b border-black w-fit">{product.brandName}</span>
                <p className="text-xs text-gray-500 line-clamp-1">{product.name}</p>
                <div className="mt-1">
                    <p className="text-sm font-bold text-black">{product.retailPrice.toLocaleString() + '원'}</p>
                    <p className="text-[10px] text-gray-400">발매가</p>
                </div>
            </div>
        </Link>
    );
};
