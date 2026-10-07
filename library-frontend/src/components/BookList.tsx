import type { Book } from '../types/book';
import type { LoadState } from '../api/useBooks';

interface BookListProps {
    books: Book[];
    state: LoadState;
    errorMessage: string;
    onRetry: () => void;
    onEdit?: (book: Book) => void;
    onDelete?: (book: Book) => void;
    onBorrow?: (book: Book) => void;
    borrowingId?: number | null;
}

export default function BookList({
    books, state, errorMessage, onRetry, onEdit, onDelete, onBorrow, borrowingId,
}: BookListProps) {
    if (state === 'loading') return <p className="muted">Đang tải danh sách sách...</p>;

    if (state === 'error') {
        return (
            <div className="error-box">
                <p>{errorMessage}</p>
                <button onClick={onRetry}>Thử lại</button>
            </div>
        );
    }

    if (state === 'empty') return <p className="muted">Không tìm thấy sách nào phù hợp.</p>;

    const showActions = !!onEdit || !!onDelete || !!onBorrow;

    return (
        <div className="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Tên sách</th>
                        <th>Tác giả</th>
                        <th>Thể loại</th>
                        <th>Năm XB</th>
                        <th>Còn lại / Tổng</th>
                        {showActions && <th>Thao tác</th>}
                    </tr>
                </thead>
                <tbody>
                    {books.map((book) => (
                        <tr key={book.id}>
                            <td>
                                <strong>{book.title}</strong>
                                <div className="muted small">ISBN: {book.isbn}</div>
                            </td>
                            <td>{book.author}</td>
                            <td>{book.categoryName}</td>
                            <td>{book.publishYear ?? '—'}</td>
                            <td className={book.availableCopies === 0 ? 'text-danger' : ''}>
                                {book.availableCopies} / {book.totalCopies}
                            </td>
                            {showActions && (
                                <td className="actions">
                                    {onEdit && <button onClick={() => onEdit(book)}>Sửa</button>}
                                    {onDelete && (
                                        <button className="danger" onClick={() => onDelete(book)}>Xóa</button>
                                    )}
                                    {onBorrow && (
                                        <button
                                            className="primary"
                                            onClick={() => onBorrow(book)}
                                            disabled={book.availableCopies === 0 || borrowingId === book.id}
                                        >
                                            {borrowingId === book.id
                                                ? 'Đang mượn...'
                                                : book.availableCopies === 0 ? 'Hết sách' : 'Mượn'}
                                        </button>
                                    )}
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
