import axiosClient from './axiosClient';
import type { PagedResponse } from '../types/book';
import type { BorrowRecord, BorrowStatus, Fine } from '../types/borrow';
import type { BorrowSummary } from '../types/borrow';

// Không gửi readerId: server luôn lấy danh tính độc giả từ JWT
export const borrowBook = (bookId: number) =>
    axiosClient.post<BorrowRecord>('/api/borrows', { bookId });

export const returnBook = (id: number) =>
    axiosClient.put<BorrowRecord>(`/api/borrows/${id}/return`);

export const getMyBorrows = (status?: BorrowStatus, overdue?: boolean) =>
    axiosClient.get<BorrowRecord[]>('/api/borrows/my', { params: { status, overdue } });

export const getMySummary = () => axiosClient.get<BorrowSummary>('/api/borrows/my/summary');

export const renewBook = (id: number) =>
    axiosClient.patch<BorrowRecord>(`/api/borrows/${id}/renew`);

export const getAllBorrows = (status?: BorrowStatus, page = 0, size = 10) =>
    axiosClient.get<PagedResponse<BorrowRecord>>('/api/borrows', {
        params: { status, page, size },
    });

export const getMyFines = () => axiosClient.get<Fine[]>('/api/fines/my');

export const getAllFines = (paid?: boolean, page = 0, size = 10) =>
    axiosClient.get<PagedResponse<Fine>>('/api/fines', {
        params: { paid, page, size },
    });

export const payFine = (id: number) => axiosClient.patch<Fine>(`/api/fines/${id}/pay`);
