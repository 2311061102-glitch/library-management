import { useState, useCallback } from 'react';
import { getAllBorrows, returnBook } from '../api/borrowApi';
import { useFetch } from '../hooks/useFetch';
import { useToast } from '../hooks/useToast';
import Toast from '../components/Toast';
import StatusBadge from '../components/StatusBadge';
import Pagination from '../components/Pagination';
import { getErrorMessage } from '../utils/errors';
import { formatDate, formatDateTime, formatVnd } from '../utils/format';
import type { BorrowRecord, BorrowStatus } from '../types/borrow';

export default function AdminBorrowsPage() {
    const [status, setStatus] = useState<BorrowStatus | ''>('BORROWING');
    const [page, setPage] = useState(0);
    const [returningId, setReturningId] = useState<number | null>(null);
    const { toast, showToast, clearToast } = useToast();

    const fetcher = useCallback(
        () => getAllBorrows(status || undefined, page).then((res) => res.data),
        [status, page]
    );
    const { data, error, loading, reload } = useFetch(fetcher, 'Không tải được danh sách phiếu mượn.');
    const rows = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;

    const handleReturn = async (row: BorrowRecord) => {
        if (!window.confirm(`Ghi nhận trả sách "${row.bookTitle}" của độc giả #${row.readerId}?`)) return;
        setReturningId(row.id);
        try {
            const res = await returnBook(row.id);
            showToast(
                res.data.estimatedFine > 0
                    ? `Đã nhận trả. Trễ ${res.data.overdueDays} ngày, phạt ${formatVnd(res.data.estimatedFine)}.`
                    : 'Đã nhận trả sách.',
                'success'
            );
            reload();
        } catch (err) {
            showToast(getErrorMessage(err, 'Ghi nhận trả sách không thành công.'), 'error');
        } finally {
            setReturningId(null);
        }
    };

    return (
        <div className="page">
            <h1>Quản lý phiếu mượn</h1>

            <div className="toolbar">
                <select value={status} onChange={(e) => { setStatus(e.target.value as BorrowStatus | ''); setPage(0); }}>
                    <option value="">Tất cả phiếu</option>
                    <option value="BORROWING">Đang mượn</option>
                    <option value="RETURNED">Đã trả</option>
                </select>
            </div>

            {loading && !data && <p className="muted">Đang tải...</p>}
            {error && <p className="field-error">{error}</p>}
            {!loading && !error && rows.length === 0 && <p className="muted">Không có phiếu mượn nào.</p>}

            {!error && rows.length > 0 && (
                <div className="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Mã phiếu</th><th>Độc giả</th><th>Tên sách</th><th>Ngày mượn</th>
                                <th>Hạn trả</th><th>Trạng thái</th><th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {rows.map((row) => (
                                <tr key={row.id}>
                                    <td>#{row.id}</td>
                                    <td>#{row.readerId}</td>
                                    <td>{row.bookTitle}</td>
                                    <td>{formatDateTime(row.borrowDate)}</td>
                                    <td>{formatDate(row.dueDate)}</td>
                                    <td><StatusBadge record={row} /></td>
                                    <td>
                                        {row.status === 'BORROWING' && (
                                            <button onClick={() => handleReturn(row)} disabled={returningId === row.id}>
                                                {returningId === row.id ? 'Đang xử lý...' : 'Nhận trả'}
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
        </div>
    );
}
