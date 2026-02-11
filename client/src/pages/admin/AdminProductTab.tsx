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

    // Modal State
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState<ProductInfo | null>(null);

    const fetchProducts = async () => {
        setIsLoading(true);
        try {
            const data = await productApi.getAll(page, 20); // 20 items per page
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
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold">상품 관리 (총 {total}개)</h2>
                <div className="flex gap-2">
                    <button
                        onClick={handleCreate}
                        className="px-4 py-2 bg-black text-white rounded font-bold hover:bg-gray-800 text-sm"
                    >
                        + 상품 등록
                    </button>
                    <button
                        onClick={fetchProducts}
                        className="px-3 py-1 bg-gray-100 rounded hover:bg-gray-200 text-sm"
                    >
                        새로고침
                    </button>
                </div>
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
                                        <span className={`px-2 py-1 rounded text-xs ${product.productStatus === 'ON_SALE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                                            {product.productStatus}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3 text-right">
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
