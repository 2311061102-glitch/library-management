export interface Category {
    id: number;
    name: string;
    description: string | null;
    bookCount: number;
}

export interface CategoryFormValues {
    name: string;
    description: string;
}

export interface Book {
    id: number;
    title: string;
    author: string;
    isbn: string;
    publishYear: number | null;
    categoryId: number;
    categoryName: string;
    totalCopies: number;
    availableCopies: number;
}

export interface PagedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export interface BookFormValues {
    title: string;
    author: string;
    isbn: string;
    publishYear: string; // dùng string trong form để dễ kiểm soát input rỗng, sẽ Number() khi gửi đi
    categoryId: string;
    totalCopies: string;
}

export const emptyBookForm: BookFormValues = {
    title: '',
    author: '',
    isbn: '',
    publishYear: '',
    categoryId: '',
    totalCopies: '',
};
