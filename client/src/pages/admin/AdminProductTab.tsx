import { useEffect, useState } from 'react';
import { Trash2, Edit } from 'lucide-react';
import { productApi, type ProductInfo } from '../../api/product'; // Removed AdminGetAllProductResponse unused import
import { adminApi } from '../../api/admin';
import ProductFormModal from './ProductFormModal';

const AdminProductTab = () => {
    const [products, setProducts] = useState<ProductInfo[]>([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(false);

    // Search & Filter State
    const [keyword, setKeyword] = useState('');
    const [statusFilter, setStatusFilter] = useState('');

    // Modal State
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState<ProductInfo | null>(null);

    const fetchProducts = async () => {
        setIsLoading(true);
        try {
            const data = await productApi.getAll(page, 20, {
                keyword: keyword || undefined,
                // Status filter implementation might depend on backend DTO support
                // For now, let's focus on keyword search which we know works.
            });
            setProducts(data.productList);
            setTotal(data.totalElements);
        } catch (error) {
            console.error(error);
            alert("상품 목록을 불러오는데 실패했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchProducts();
    }, [page]);

    const handleSearchSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setPage(0);
        fetchProducts();
    };

    const handleDelete = async (id: number) => {
        if (!confirm("정말 이 상품을 삭제하시겠습니까?")) return;
        try {
            await adminApi.deleteProduct(id);
            alert("상품이 삭제되었습니다.");
            fetchProducts();
        } catch (error) {
            console.error(error);
            alert("상품 삭제에 실패했습니다.");
        }
    };

    const handleCreate = () => {
        setSelectedProduct(null);
        setIsModalOpen(true);
    };

    const handleEdit = (product: ProductInfo) => {
        setSelectedProduct(product);
        setIsModalOpen(true);
    };

    const handleModalSubmit = () => {
        fetchProducts();
    };

    return (
        <div>
            <div className="flex flex-col gap-4 mb-6">
                <div className="flex justify-between items-center">
                    <h2 className="text-xl font-bold">상품 관리 (총 {total}개)</h2>
                    <div className="flex gap-2">
                        <button
                            onClick={handleCreate}
                            className="px-4 py-2 bg-black text-white rounded font-bold hover:bg-gray-800 text-sm"
                        >
                            + 상품 등록
                        </button>
                    </div>
                </div>

                {/* Admin Search Bar */}
                <form onSubmit={handleSearchSubmit} className="flex gap-2">
                    <div className="relative flex-1">
                        <input
                            type="text"
                            placeholder="상품명, 모델번호로 검색..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            className="w-full pl-3 pr-10 py-2 border border-gray-200 rounded-lg text-sm focus:ring-1 focus:ring-black outline-none"
                        />
                        <button type="submit" className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-black">
                            Search
                        </button>
                    </div>
                    <select
                        value={statusFilter}
                        onChange={(e) => {
                            setStatusFilter(e.target.value);
                            // Auto-fetch if wanted, or wait for search submit
                        }}
                        className="px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none bg-white"
                    >
                        <option value="">전체 상태</option>
                        <option value="ON_SALE">ON_SALE</option>
                        <option value="OUT_OF_STOCK">OUT_OF_STOCK</option>
                        <option value="HIDDEN">HIDDEN</option>
                    </select>
                    <button
                        type="button"
                        onClick={() => {
                            setKeyword('');
                            setStatusFilter('');
                            setPage(0);
                        }}
                        className="px-3 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 text-sm font-medium"
                    >
                        초기화
                    </button>
                </form>
            </div>

            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
                <table className="w-full text-sm text-left">
                    <thead className="bg-gray-50 text-gray-500 font-medium border-b border-gray-200">
                        <tr>
                            <th className="px-4 py-3 w-16">ID</th>
                            <th className="px-4 py-3">상품명</th>
                            <th className="px-4 py-3">모델번호</th>
                            <th className="px-4 py-3">발매가</th>
                            <th className="px-4 py-3">상태</th>
                            <th className="px-4 py-3 text-right">관리</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {isLoading ? (
                            <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
                        ) : products.length === 0 ? (
                            <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">등록된 상품이 없습니다.</td></tr>
                        ) : (
                            products.map(product => (
                                <tr key={product.productId} className="hover:bg-gray-50">
                                    <td className="px-4 py-3 text-gray-500">{product.productId}</td>
                                    <td className="px-4 py-3 font-medium">{product.name}</td>
                                    <td className="px-4 py-3 text-gray-500">{product.modelNumber}</td>
                                    <td className="px-4 py-3">{product.retailPrice.toLocaleString()}원</td>
                                    <td className="px-4 py-3">
                                        {product.deletedAt ? (
                                            <span className="px-2 py-1 bg-red-50 text-red-600 rounded text-xs font-bold">
                                                REMOVED
                                            </span>
                                        ) : (
                                            <span className={`px-2 py-1 rounded text-xs ${product.productStatus === 'ON_SALE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                                                {product.productStatus}
                                            </span>
                                        )}
                                    </td>
                                    <td className="px-4 py-3 text-right">
                                        {!product.deletedAt && (
                                            <div className="flex justify-end gap-2">
                                                <button
                                                    onClick={() => handleEdit(product)}
                                                    className="p-1 text-blue-500 hover:bg-blue-50 rounded"
                                                    title="수정"
                                                >
                                                    <Edit size={16} />
                                                </button>
                                                <button
                                                    onClick={() => handleDelete(product.productId)}
                                                    className="p-1 text-red-500 hover:bg-red-50 rounded"
                                                    title="삭제"
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Simple Pagination */}
            <div className="flex justify-center gap-2 mt-4">
                <button
                    disabled={page === 0}
                    onClick={() => setPage(p => p - 1)}
                    className="px-3 py-1 border rounded disabled:opacity-50"
                >
                    이전
                </button>
                <span className="px-3 py-1">Page {page + 1}</span>
                <button
                    onClick={() => setPage(p => p + 1)}
                    className="px-3 py-1 border rounded"
                >
                    다음
                </button>
            </div>

            <ProductFormModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={handleModalSubmit}
                initialData={selectedProduct}
            />
        </div>
    );
};

export default AdminProductTab;
