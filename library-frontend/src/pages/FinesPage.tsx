import { useState, useCallback } from 'react';
import { getMyFines, getAllFines, payFine } from '../api/borrowApi';
import { useFetch } from '../hooks/useFetch';
import { useToast } from '../hooks/useToast';
import Toast from '../components/Toast';
import Pagination from '../components/Pagination';
import { getErrorMessage } from '../utils/errors';
import { formatDateTime, formatVnd } from '../utils/format';
import type { Fine } from '../types/borrow';

interface FinesPageProps {
    mode: 'reader' | 'librarian'; // reader: xem phạt của mình; librarian: xem tất cả + xác nhận thu phạt
}

type PaidFilter = 'all' | 'unpaid' | 'paid';

export default function FinesPage({ mode }: FinesPageProps) {
    const isLibrarian = mode === 'librarian';
    const [paidFilter, setPaidFilter] = useState<PaidFilter>('unpaid');
    const [page, setPage] = useState(0);
    const [payingId, setPayingId] = useState<number | null>(null);
    const { toast, showToast, clearToast } = useToast();

    const fetcher = useCallback(async () => {
        if (isLibrarian) {
            const paid = paidFilter === 'all' ? undefined : paidFilter === 'paid';
            const res = await getAllFines(paid, page);
            return { rows: res.data.content, totalPages: res.data.totalPages };
        }
        const res = await getMyFines();
        return { rows: res.data, totalPages: 0 };
    }, [isLibrarian, paidFilter, page]);

    const { data, error, loading, reload } = useFetch(fetcher, 'Không tải được danh sách tiền phạt.');
    const rows: Fine[] = data?.rows ?? [];
    const totalPages = data?.totalPages ?? 0;

    const handlePay = async (fine: Fine) => {
        if (!window.confirm(`Xác nhận đã thu ${formatVnd(fine.amount)} của độc giả #${fine.readerId}?`)) return;
        setPayingId(fine.id);
        try {
            await payFine(fine.id);
            showToast('Đã ghi nhận thanh toán tiền phạt.', 'success');
            reload();
        } catch (err) {
            showToast(getErrorMessage(err, 'Ghi nhận thanh toán không thành công.'), 'error');
        } finally {
            setPayingId(null);
        }
    };

    const unpaidTotal = rows.filter((f) => !f.paid).reduce((sum, f) => sum + f.amount, 0);

    return (
        <div className="page">
            <h1>{isLibrarian ? 'Quản lý tiền phạt' : 'Tiền phạt của tôi'}</h1>

            {isLibrarian ? (
                <div className="toolbar">
                    <select value={paidFilter}
                            onChange={(e) => { setPaidFilter(e.target.value as PaidFilter); setPage(0); }}>
                        <option value="unpaid">Chưa thanh toán</option>
                        <option value="paid">Đã thanh toán</option>
                        <option value="all">Tất cả</option>
                    </select>
                </div>
            ) : (
                unpaidTotal > 0 && (
                    <p className="error-box">
                        Bạn còn nợ <strong>{formatVnd(unpaidTotal)}</strong>. Vui lòng nộp phạt tại quầy thủ thư
                        để tiếp tục mượn sách.
                    </p>
                )
            )}

            {loading && !data && <p className="muted">Đang tải...</p>}
            {error && <p className="field-error">{error}</p>}
            {!loading && !error && rows.length === 0 && <p className="muted">Không có khoản phạt nào.</p>}

            {!error && rows.length > 0 && (
                <div className="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                {isLibrarian && <th>Độc giả</th>}
                                <th>Tên sách</th><th>Lý do</th><th>Số tiền</th>
                                <th>Ngày lập</th><th>Trạng thái</th>
                                {isLibrarian && <th>Thao tác</th>}
                            </tr>
                        </thead>
                        <tbody>
                            {rows.map((fine) => (
                                <tr key={fine.id}>
                                    {isLibrarian && <td>#{fine.readerId}</td>}
                                    <td>{fine.bookTitle}</td>
                                    <td>{fine.reason}</td>
                                    <td>{formatVnd(fine.amount)}</td>
                                    <td>{formatDateTime(fine.createdAt)}</td>
                                    <td>
                                        {fine.paid
                                            ? <span className="badge badge-ok">Đã thanh toán</span>
                                            : <span className="badge badge-danger">Chưa thanh toán</span>}
                                    </td>
                                    {isLibrarian && (
                                        <td>
                                            {!fine.paid && (
                                                <button className="primary" onClick={() => handlePay(fine)}
                                                        disabled={payingId === fine.id}>
                                                    {payingId === fine.id ? 'Đang xử lý...' : 'Xác nhận đã thu'}
                                                </button>
                                            )}
                                        </td>
                                    )}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {isLibrarian && <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />}
            {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
        </div>
    );
}
