import { useState, useEffect } from 'react';
import { X, Upload } from 'lucide-react';
import { adminApi } from '../../api/admin';
import { productApi, type ProductInfo, type ProductOptionInfo } from '../../api/product';

interface ProductFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: () => void;
    initialData?: ProductInfo | null;
}

const ProductFormModal = ({ isOpen, onClose, onSubmit, initialData }: ProductFormModalProps) => {
    const [formData, setFormData] = useState({
        name: '',
        modelNumber: '',
        brandName: '',
        categoryId: 1,
        imageIds: [1], // Dummy ID as backend ignores this but requires @NotNull
        sizes: [] as string[],
        color: '',
        sizeUnit: 'mm',
        productStatus: 'ON_SALE',
        operationStatus: 'ACTIVE',
        retailPrice: 0,
        retailDate: new Date().toISOString().split('T')[0] // YYYY-MM-DD
    });
    const [sizeInput, setSizeInput] = useState('');
    const [imagePreview, setImagePreview] = useState<string | null>(null);

    useEffect(() => {
        const loadFullData = async () => {
            if (initialData && isOpen) {
                try {
                    const fullData = await productApi.getOne(initialData.productId);
                    setFormData({
                        name: fullData.name,
                        modelNumber: fullData.modelNumber,
                        brandName: fullData.brandName,
                        categoryId: fullData.categoryId,
                        imageIds: fullData.imageIds.length > 0 ? fullData.imageIds : [1],
                        sizes: fullData.options.map((opt: ProductOptionInfo) => opt.size),
                        color: fullData.color,
                        sizeUnit: fullData.sizeUnit,
                        productStatus: fullData.productStatus,
                        operationStatus: fullData.operationStatus,
                        retailPrice: fullData.retailPrice,
                        retailDate: fullData.retailDate.substring(0, 10)
                    });
                } catch (error) {
                    console.error("Failed to fetch full product details", error);
                    // Fallback to partial data if fetch fails
                    setFormData({
                        name: initialData.name,
                        modelNumber: initialData.modelNumber,
                        brandName: '',
                        categoryId: initialData.categoryId,
                        imageIds: [1],
                        sizes: [],
                        color: '',
                        sizeUnit: 'mm',
                        productStatus: initialData.productStatus,
                        operationStatus: initialData.operationStatus,
                        retailPrice: initialData.retailPrice,
                        retailDate: new Date().toISOString().split('T')[0]
                    });
                }
            } else if (isOpen) {
                // Reset for create
                setFormData({
                    name: '',
                    modelNumber: '',
                    brandName: '',
                    categoryId: 1,
                    imageIds: [1],
                    sizes: [],
                    color: '',
                    sizeUnit: 'mm',
                    productStatus: 'ON_SALE',
                    operationStatus: 'ACTIVE',
                    retailPrice: 0,
                    retailDate: new Date().toISOString().split('T')[0]
                });
                setImagePreview(null);
            }
        };

        loadFullData();
    }, [initialData, isOpen]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'retailPrice' || name === 'categoryId' ? Number(value) : value
        }));
    };

    const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            try {
                const file = e.target.files[0];
                const urls = await adminApi.uploadImage(file);
                if (urls && urls.length > 0) {
                    setImagePreview(urls[0]);
                    // Note: Backend CreateProductRequest expects IDs, but ImageController returns URLs.
                    // Also backend service currently ignores imageList.
                    // So we just keep the dummy ID [1] in formData.
                }
            } catch (error) {
                console.error("Image upload failed", error);
                alert("이미지 업로드에 실패했습니다.");
            }
        }
    };

    const handleAddSize = () => {
        if (sizeInput && !formData.sizes.includes(sizeInput)) {
            setFormData(prev => ({
                ...prev,
                sizes: [...prev.sizes, sizeInput]
            }));
            setSizeInput('');
        }
    };

    const handleRemoveSize = (sizeToRemove: string) => {
        setFormData(prev => ({
            ...prev,
            sizes: prev.sizes.filter(s => s !== sizeToRemove)
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const payload = {
                ...formData,
                retailDate: formData.retailDate + "T00:00:00" // Append time for LocalDateTime
            };

            if (initialData) {
                // Update
                // Update API expects options, not sizes? Let's check DTO.
                // AdminUpdateProductRequest has 'options' (List<String>)
                // AdminCreateProductRequest has 'sizes' (List<String>)
                // We need to map properly.
                const updatePayload = {
                    ...payload,
                    options: payload.sizes
                };
                // Remove 'sizes' from payload? API usually ignores extra fields but let's be safe.
                await adminApi.updateProduct(initialData.productId, updatePayload);
                alert("상품이 수정되었습니다.");
            } else {
                // Create
                await adminApi.createProduct(payload);
                alert("상품이 등록되었습니다.");
            }
            onClose();
            onSubmit();
        } catch (error) {
            console.error("Submit failed", error);
            alert("상품 저장에 실패했습니다.");
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
            <div className="bg-white rounded-xl w-[600px] max-h-[90vh] overflow-y-auto">
                <div className="flex justify-between items-center p-6 border-b border-gray-100">
                    <h2 className="text-xl font-bold">{initialData ? '상품 수정' : '상품 등록'}</h2>
                    <button onClick={onClose}><X size={24} /></button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 flex flex-col gap-4">
                    {/* Basic Info */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-bold mb-1">브랜드명</label>
                            <input name="brandName" value={formData.brandName} onChange={handleChange} className="w-full border p-2 rounded" required />
                        </div>
                        <div>
                            <label className="block text-sm font-bold mb-1">모델번호</label>
                            <input name="modelNumber" value={formData.modelNumber} onChange={handleChange} className="w-full border p-2 rounded" required />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-bold mb-1">상품명</label>
                        <input name="name" value={formData.name} onChange={handleChange} className="w-full border p-2 rounded" required />
                    </div>

                    {/* Image Upload */}
                    <div>
                        <label className="block text-sm font-bold mb-1">상품 이미지</label>
                        <div className="flex items-center gap-4">
                            <div className="w-24 h-24 bg-gray-100 rounded border flex items-center justify-center overflow-hidden">
                                {imagePreview ? (
                                    <img src={imagePreview} alt="Preview" className="w-full h-full object-cover" />
                                ) : (
                                    <span className="text-gray-400 text-xs">No Image</span>
                                )}
                            </div>
                            <label className="flex items-center gap-2 px-4 py-2 border rounded cursor-pointer hover:bg-gray-50">
                                <Upload size={16} />
                                <span>이미지 업로드</span>
                                <input type="file" className="hidden" onChange={handleImageUpload} accept="image/*" />
                            </label>
                        </div>
                    </div>

                    {/* Category & Status */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-bold mb-1">카테고리 ID</label>
                            <input type="number" name="categoryId" value={formData.categoryId} onChange={handleChange} className="w-full border p-2 rounded" required />
                        </div>
                        <div>
                            <label className="block text-sm font-bold mb-1">발매가</label>
                            <input type="number" name="retailPrice" value={formData.retailPrice} onChange={handleChange} className="w-full border p-2 rounded" required />
                        </div>
                    </div>

                    {/* Enum Status Selectors */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-bold mb-1">운영 상태</label>
                            <select
                                name="operationStatus"
                                value={formData.operationStatus}
                                onChange={handleChange}
                                className="w-full border p-2 rounded bg-white shadow-sm focus:ring-2 focus:ring-black outline-none"
                            >
                                <option value="ACTIVE">운영 중 (ACTIVE)</option>
                                <option value="INACTIVE">운영 중지 (INACTIVE)</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-bold mb-1">판매 상태</label>
                            <select
                                name="productStatus"
                                value={formData.productStatus}
                                onChange={handleChange}
                                className="w-full border p-2 rounded bg-white shadow-sm focus:ring-2 focus:ring-black outline-none"
                            >
                                <option value="ON_SALE">판매 중 (ON_SALE)</option>
                                <option value="NO_LISTING">등록 상품 없음 (NO_LISTING)</option>
                            </select>
                        </div>
                    </div>

                    {/* Options (Sizes) */}
                    <div>
                        <label className="block text-sm font-bold mb-1">사이즈 (옵션)</label>
                        <div className="flex gap-2 mb-2">
                            <input
                                value={sizeInput}
                                onChange={(e) => setSizeInput(e.target.value)}
                                className="border p-2 rounded flex-1"
                                placeholder="예: 260"
                            />
                            <button type="button" onClick={handleAddSize} className="px-4 py-2 bg-black text-white rounded">추가</button>
                        </div>
                        <div className="flex flex-wrap gap-2">
                            {formData.sizes.map(size => (
                                <span key={size} className="px-3 py-1 bg-gray-100 rounded-full text-sm flex items-center gap-2">
                                    {size}
                                    <button type="button" onClick={() => handleRemoveSize(size)}><X size={14} /></button>
                                </span>
                            ))}
                        </div>
                        {formData.sizes.length === 0 && <p className="text-xs text-red-500">최소 1개의 사이즈를 추가해야 합니다.</p>}
                    </div>

                    {/* Additional Info */}
                    <div className="grid grid-cols-3 gap-4">
                        <div>
                            <label className="block text-sm font-bold mb-1">컬러</label>
                            <input name="color" value={formData.color} onChange={handleChange} className="w-full border p-2 rounded" />
                        </div>
                        <div>
                            <label className="block text-sm font-bold mb-1">사이즈 단위</label>
                            <input name="sizeUnit" value={formData.sizeUnit} onChange={handleChange} className="w-full border p-2 rounded" placeholder="mm" />
                        </div>
                        <div>
                            <label className="block text-sm font-bold mb-1">발매일</label>
                            <input type="date" name="retailDate" value={formData.retailDate} onChange={handleChange} className="w-full border p-2 rounded" />
                        </div>
                    </div>

                    <button type="submit" className="mt-4 w-full py-3 bg-black text-white font-bold rounded-lg hover:bg-gray-800">
                        {initialData ? '수정하기' : '등록하기'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ProductFormModal;
