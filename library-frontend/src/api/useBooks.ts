import { useCallback } from 'react';
import { getBooks } from './bookApi';
import { useFetch } from '../hooks/useFetch';

export type LoadState = 'loading' | 'success' | 'empty' | 'error';

export function useBooks(keyword: string, categoryId: number | undefined, page: number, size = 10) {
    const fetcher = useCallback(
        () => getBooks(keyword, categoryId, page, size).then((res) => res.data),
        [keyword, categoryId, page, size]
    );
    const { data, error, loading, reload } = useFetch(
        fetcher, 'Đã xảy ra lỗi không xác định, vui lòng thử lại.'
    );

    const books = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;

    let state: LoadState;
    if (error) state = 'error';
    else if (loading && !data) state = 'loading';
    else state = books.length === 0 ? 'empty' : 'success';

    return { books, totalPages, state, errorMessage: error ?? '', refetch: reload };
}
