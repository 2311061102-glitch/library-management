import { useState, useCallback } from 'react';
import { useBooks } from '../api/useBooks';
import { useCategories } from '../api/useCategories';
import { createBook, updateBook, deleteBook } from '../api/bookApi';
import SearchBox from '../components/SearchBox';
import CategoryFilter from '../components/CategoryFilter';
import BookList from '../components/BookList';
import Pagination from '../components/Pagination';
import BookForm from '../components/BookForm';
import CategoryPanel from '../components/CategoryPanel';
import { getErrorMessage } from '../utils/errors';
import type { Book, BookFormValues } from '../types/book';

export default function AdminBooksPage() {
    const [keyword, setKeyword] = useState('');
    const [categoryId, setCategoryId] = useState<number | undefined>();
    const [page, setPage] = useState(0);
    const [editingBook, setEditingBook] = useState<Book | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState<string | null>(null);
    const [formVersion, setFormVersion] = useState(0); // tăng sau mỗi lần lưu để làm sạch form

    const { categories, refetchCategories } = useCategories();
    const { books, totalPages, state, errorMessage, refetch } = useBooks(keyword, categoryId, page);

    const handleSearch = useCallback((kw: string) => {
        setKeyword(kw);
        setPage(0);
    }, []);

    const handleFormSubmit = async (values: BookFormValues) => {
        setSubmitting(true);
        setFormError(null);
        try {
            if (editingBook) await updateBook(editingBook.id, values);
            else await createBook(values);
            setEditingBook(null);
            setFormVersion((v) => v + 1);
            refetch();
            refetchCategories(); // cập nhật cột "Số sách" của thể loại
        } catch (err) {
            setFormError(getErrorMessage(err, 'Đã xảy ra lỗi, vui lòng thử lại.'));
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (book: Book) => {
        if (!window.confirm(`Xóa sách "${book.title}"?`)) return;
        try {
            await deleteBook(book.id);
            refetch();
            refetchCategories();
        } catch (err) {
            alert(getErrorMessage(err, 'Xóa sách không thành công.'));
        }
    };

    return (
        <div className="page">
            <h1>Quản lý sách (Thủ thư)</h1>

            <BookForm
                key={`${editingBook?.id ?? 'new'}-${formVersion}`}
                editingBook={editingBook}
                categories={categories}
                onSubmit={handleFormSubmit}
                onCancel={() => setEditingBook(null)}
                submitting={submitting}
                serverError={formError}
            />

            <div className="toolbar">
                <SearchBox onSearch={handleSearch} />
                <CategoryFilter categories={categories} value={categoryId}
                                onChange={(id) => { setCategoryId(id); setPage(0); }} />
            </div>
            <BookList books={books} state={state} errorMessage={errorMessage} onRetry={refetch}
                      onEdit={setEditingBook} onDelete={handleDelete} />
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />

            <CategoryPanel categories={categories} onChanged={() => { refetchCategories(); refetch(); }} />
        </div>
    );
}
