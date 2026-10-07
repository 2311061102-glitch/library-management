import { useState } from 'react';
import { getMyBorrows, returnBook } from '../api/borrowApi';
import { useFetch } from '../hooks/useFetch';
import { useToast } from '../hooks/useToast';
import Toast from '../components/Toast';
import StatusBadge from '../components/StatusBadge';
import { getErrorMessage } from '../utils/errors';
import { formatDate, formatDateTime, formatVnd } from '../utils/format';
import type { BorrowRecord } from '../types/borrow';

const fetchMyBorrows = () => getMyBorrows().then((res) => res.data);

export default function MyBorrowsPage() {
    const [onlyBorrowing, setOnlyBorrowing] = useState(true);
    const [returningId, setReturningId] = useState<number | null>(null);
    const { toast, showToast, clearToast } = useToast();

    const { data, error, loading, reload } = useFetch(fetchMyBorrows, 'Không tải được danh sách mượn.');
    const rows = data ?? [];

    const handleReturn = async (row: BorrowRecord) => {
        if (!window.confirm(`Trả sách "${row.bookTitle}"?`)) return;
        setReturningId(row.id);
        try {
            const res = await returnBook(row.id);
            const fine = res.data.estimatedFine;
            showToast(
                fine > 0
                    ? `Đã trả "${row.bookTitle}". Trả trễ ${res.data.overdueDays} ngày, tiền phạt ${formatVnd(fine)}.`
                    : `Đã trả "${row.bookTitle}". Cảm ơn bạn!`,
                fine > 0 ? 'error' : 'success'
            );
            reload();
        } catch (err) {
            showToast(getErrorMessage(err, 'Trả sách không thành công.'), 'error');
        } finally {
            setReturningId(null);
        }
    };

    const visible = onlyBorrowing ? rows.filter((r) => r.status === 'BORROWING') : rows;

    return (
        <div className="page">
            <h1>Sách đang mượn</h1>

            <label className="checkbox">
                <input type="checkbox" checked={onlyBorrowing} onChange={(e) => setOnlyBorrowing(e.target.checked)} />
                Chỉ hiện sách đang mượn
            </label>

            {loading && !data && <p className="muted">Đang tải...</p>}
            {error && <p className="field-error">{error}</p>}
            {!loading && !error && visible.length === 0 && <p className="muted">Không có phiếu mượn nào.</p>}

            {!error && visible.length > 0 && (
                <div className="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Tên sách</th><th>Ngày mượn</th><th>Hạn trả</th>
                                <th>Trạng thái</th><th>Tiền phạt</th><th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {visible.map((row) => (
                                <tr key={row.id}>
                                    <td>{row.bookTitle}</td>
                                    <td>{formatDateTime(row.borrowDate)}</td>
                                    <td>{formatDate(row.dueDate)}</td>
                                    <td><StatusBadge record={row} /></td>
                                    <td className={row.estimatedFine > 0 ? 'text-danger' : ''}>
                                        {row.estimatedFine > 0 ? formatVnd(row.estimatedFine) : '—'}
                                    </td>
                                    <td>
                                        {row.status === 'BORROWING' && (
                                            <button onClick={() => handleReturn(row)} disabled={returningId === row.id}>
                                                {returningId === row.id ? 'Đang trả...' : 'Trả sách'}
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
            {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
        </div>
    );
}
