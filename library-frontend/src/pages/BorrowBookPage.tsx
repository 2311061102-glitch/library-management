import { useState, useCallback } from 'react';
import { useBooks } from '../api/useBooks';
import { useCategories } from '../api/useCategories';
import { borrowBook } from '../api/borrowApi';
import { useToast } from '../hooks/useToast';
import SearchBox from '../components/SearchBox';
import CategoryFilter from '../components/CategoryFilter';
import BookList from '../components/BookList';
import Pagination from '../components/Pagination';
import Toast from '../components/Toast';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import type { Book } from '../types/book';

export default function BorrowBookPage() {
    const [keyword, setKeyword] = useState('');
    const [categoryId, setCategoryId] = useState<number | undefined>();
    const [page, setPage] = useState(0);
    const [borrowingId, setBorrowingId] = useState<number | null>(null);
    const { toast, showToast, clearToast } = useToast();

    const { categories } = useCategories();
    const { books, totalPages, state, errorMessage, refetch } = useBooks(keyword, categoryId, page);

    const handleSearch = useCallback((kw: string) => {
        setKeyword(kw);
        setPage(0);
    }, []);

    const handleBorrow = async (book: Book) => {
        setBorrowingId(book.id);
        try {
            const res = await borrowBook(book.id);
            showToast(`Mượn thành công "${book.title}". Hạn trả: ${formatDate(res.data.dueDate)}`, 'success');
            refetch();
        } catch (err) {
            showToast(getErrorMessage(err, 'Mượn sách không thành công, vui lòng thử lại.'), 'error');
        } finally {
            setBorrowingId(null);
        }
    };

    return (
        <div className="page">
            <h1>Mượn sách</h1>
            <div className="toolbar">
                <SearchBox onSearch={handleSearch} />
                <CategoryFilter categories={categories} value={categoryId}
                                onChange={(id) => { setCategoryId(id); setPage(0); }} />
            </div>
            <BookList books={books} state={state} errorMessage={errorMessage} onRetry={refetch}
                      onBorrow={handleBorrow} borrowingId={borrowingId} />
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
        </div>
    );
}
