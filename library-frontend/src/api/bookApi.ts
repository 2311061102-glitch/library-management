import axiosClient from './axiosClient';
import type {
    Book, BookFormValues, Category, CategoryFormValues, PagedResponse,
} from '../types/book';

export const getBooks = (keyword?: string, categoryId?: number, page = 0, size = 10) =>
    axiosClient.get<PagedResponse<Book>>('/api/books', {
        params: { keyword: keyword || undefined, categoryId, page, size },
    });

export const getBookById = (id: number) => axiosClient.get<Book>(`/api/books/${id}`);

const toBookPayload = (v: BookFormValues) => ({
    title: v.title.trim(),
    author: v.author.trim(),
    isbn: v.isbn.trim(),
    publishYear: v.publishYear ? Number(v.publishYear) : null,
    categoryId: Number(v.categoryId),
    totalCopies: Number(v.totalCopies),
});

export const createBook = (values: BookFormValues) =>
    axiosClient.post<Book>('/api/books', toBookPayload(values));

export const updateBook = (id: number, values: BookFormValues) =>
    axiosClient.put<Book>(`/api/books/${id}`, toBookPayload(values));

export const deleteBook = (id: number) => axiosClient.delete(`/api/books/${id}`);

// ---------- Thể loại ----------
export const getCategories = () => axiosClient.get<Category[]>('/api/categories');

export const createCategory = (values: CategoryFormValues) =>
    axiosClient.post<Category>('/api/categories', {
        name: values.name.trim(),
        description: values.description.trim() || null,
    });

export const updateCategory = (id: number, values: CategoryFormValues) =>
    axiosClient.put<Category>(`/api/categories/${id}`, {
        name: values.name.trim(),
        description: values.description.trim() || null,
    });

export const deleteCategory = (id: number) => axiosClient.delete(`/api/categories/${id}`);
