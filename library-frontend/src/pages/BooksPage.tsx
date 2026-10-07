import { useState, useCallback } from 'react';
import { useBooks } from '../api/useBooks';
import { useCategories } from '../api/useCategories';
import { borrowBook } from '../api/borrowApi';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../hooks/useToast';
import SearchBox from '../components/SearchBox';
import CategoryFilter from '../components/CategoryFilter';
import BookList from '../components/BookList';
import Pagination from '../components/Pagination';
import Toast from '../components/Toast';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import type { Book } from '../types/book';

export default function BooksPage() {
    const [keyword, setKeyword] = useState('');
    const [categoryId, setCategoryId] = useState<number | undefined>();
    const [page, setPage] = useState(0);
    const [borrowingId, setBorrowingId] = useState<number | null>(null);
    const { isAuthenticated } = useAuth();
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
            const response = await borrowBook(book.id);
            showToast(`Đã mượn "${book.title}". Hạn trả: ${formatDate(response.data.dueDate)}.`, 'success');
            refetch();
        } catch (error) {
            showToast(getErrorMessage(error, 'Mượn sách không thành công.'), 'error');
        } finally {
            setBorrowingId(null);
        }
    };

    return (
        <div className="page">
            <h1>Tra cứu sách</h1>
            <div className="toolbar">
                <SearchBox onSearch={handleSearch} />
                <CategoryFilter categories={categories} value={categoryId}
                                onChange={(id) => { setCategoryId(id); setPage(0); }} />
            </div>
            <BookList
                books={books}
                state={state}
                errorMessage={errorMessage}
                onRetry={refetch}
                onBorrow={isAuthenticated ? handleBorrow : undefined}
                borrowingId={borrowingId}
            />
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
        </div>
    );
}
