import { useState, useCallback } from 'react';
import { useBooks } from '../api/useBooks';
import { useCategories } from '../api/useCategories';
import SearchBox from '../components/SearchBox';
import CategoryFilter from '../components/CategoryFilter';
import BookList from '../components/BookList';
import Pagination from '../components/Pagination';

export default function BooksPage() {
    const [keyword, setKeyword] = useState('');
    const [categoryId, setCategoryId] = useState<number | undefined>();
    const [page, setPage] = useState(0);

    const { categories } = useCategories();
    const { books, totalPages, state, errorMessage, refetch } = useBooks(keyword, categoryId, page);

    const handleSearch = useCallback((kw: string) => {
        setKeyword(kw);
        setPage(0);
    }, []);

    return (
        <div className="page">
            <h1>Tra cứu sách</h1>
            <div className="toolbar">
                <SearchBox onSearch={handleSearch} />
                <CategoryFilter categories={categories} value={categoryId}
                                onChange={(id) => { setCategoryId(id); setPage(0); }} />
            </div>
            <BookList books={books} state={state} errorMessage={errorMessage} onRetry={refetch} />
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
    );
}
