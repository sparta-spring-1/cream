import client from './client';

export interface ProductInfo {
    productId: number;
    name: string;
    modelNumber: string;
    categoryId: number;
    retailPrice: number;
    productStatus: string;
    operationStatus: string;
}

export interface AdminGetAllProductResponse {
    productList: ProductInfo[];
    hasNext: boolean;
    totalElements: number;
}

export interface ProductOptionInfo {
    id: number;
    size: string;
}

export interface AdminGetOneProductResponse {
    id: number;
    name: string;
    modelNumber: string;
    brandName: string;
    categoryId: number;
    imageIds: number[];
    options: ProductOptionInfo[];
    color: string;
    sizeUnit: string;
    productStatus: string;
    operationStatus: string;
    retailPrice: number;
    retailDate: string;
    createdAt: string;
    updatedAt: string;
}

export const productApi = {
    getAll: async (page = 0, size = 10, keyword?: string) => {
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('pageSize', size.toString());
        if (keyword) params.append('keyword', keyword);

        const response = await client.get<AdminGetAllProductResponse>(`/v1/admin/products?${params.toString()}`);
        return response.data;
    },

    getOne: async (id: number) => {
        const response = await client.get<AdminGetOneProductResponse>(`/v1/admin/products/${id}`);
        return response.data;
    },

    // Public APIs
    getPublicProducts: async (page = 0, size = 10, keyword?: string) => {
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('pageSize', size.toString());
        if (keyword) params.append('keyword', keyword);
        params.append('productSize', ''); // Fix for backend parameter expectation

        const response = await client.get<GetAllProductResponse>(`/v1/products?${params.toString()}`);
        return response.data;
    },

    getPublicProduct: async (id: number) => {
        const response = await client.get<GetOneProductResponse>(`/v1/products/${id}`);
        return response.data;
    }
};

export interface PublicSummaryProduct {
    productId: number;
    name: string;
    brandName: string;
    retailPrice: number;
}

export interface GetAllProductResponse {
    productList: PublicSummaryProduct[];
    hasNext: boolean;
    totalElements: number;
}

export interface GetOneProductResponse {
    id: number;
    name: string;
    modelNumber: string;
    brandName: string;
    categoryId: number;
    imageIds: number[];
    options: ProductOptionInfo[];
    color: string;
    sizeUnit: string;
    retailPrice: number;
    retailDate: string;
}

