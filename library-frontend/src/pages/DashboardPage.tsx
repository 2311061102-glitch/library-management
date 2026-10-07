import { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyBorrows, getMySummary, renewBook, returnBook } from '../api/borrowApi';
import { useFetch } from '../hooks/useFetch';
import { useToast } from '../hooks/useToast';
import Toast from '../components/Toast';
import StatusBadge from '../components/StatusBadge';
import { getErrorMessage } from '../utils/errors';
import { formatDate, formatVnd } from '../utils/format';
import type { BorrowRecord } from '../types/borrow';

export default function DashboardPage() {
    const { toast, showToast, clearToast } = useToast();
    const [busyId, setBusyId] = useState<number | null>(null);
    const summary = useFetch(() => getMySummary().then((r) => r.data), 'Không tải được tổng quan.');
    const borrows = useFetch(() => getMyBorrows('BORROWING').then((r) => r.data), 'Không tải được sách đang mượn.');
    const active = borrows.data ?? [];

    const action = useCallback(async (record: BorrowRecord, type: 'renew' | 'return') => {
        if (type === 'return' && !window.confirm(`Xác nhận trả "${record.bookTitle}"?`)) return;
        setBusyId(record.id);
        try {
            const response = type === 'renew' ? await renewBook(record.id) : await returnBook(record.id);
            showToast(type === 'renew' ? `Đã gia hạn đến ${formatDate(response.data.dueDate)}.` : 'Đã ghi nhận trả sách.', 'success');
            borrows.reload();
            summary.reload();
        } catch (error) {
            showToast(getErrorMessage(error, 'Thao tác không thành công.'), 'error');
        } finally {
            setBusyId(null);
        }
    }, [borrows, summary, showToast]);

    const stats = summary.data;
    return (
        <div className="page dashboard-page">
            <section className="welcome-hero">
                <div><p className="eyebrow">KHÔNG GIAN CỦA BẠN</p><h1>Xin chào, sẵn sàng đọc gì hôm nay?</h1><p className="hero-copy">Khám phá kho sách, quản lý phiếu mượn và theo dõi hạn trả của bạn.</p><Link className="button primary" to="/books">Khám phá kho sách <span>→</span></Link></div>
                <div className="hero-art"><span>✦</span><span>◌</span><span>▱</span></div>
            </section>
            <section className="stat-grid">
                <div className="stat-card"><span className="stat-icon blue">↗</span><div><span>Đang mượn</span><strong>{stats?.activeBorrows ?? '—'}</strong><small>cuốn sách</small></div></div>
                <div className="stat-card"><span className="stat-icon violet">◷</span><div><span>Đã trả</span><strong>{stats?.returnedBorrows ?? '—'}</strong><small>lượt mượn</small></div></div>
                <div className="stat-card"><span className="stat-icon amber">!</span><div><span>Đang quá hạn</span><strong>{stats?.overdueBorrows ?? '—'}</strong><small>cần xử lý</small></div></div>
                <div className="stat-card"><span className="stat-icon rose">₫</span><div><span>Phạt chưa thanh toán</span><strong>{stats ? formatVnd(stats.unpaidFineAmount) : '—'}</strong><small>{stats?.unpaidFineCount ?? 0} khoản</small></div></div>
            </section>
            <section className="section-heading"><div><p className="eyebrow">ĐANG TRONG TAY BẠN</p><h2>Sách đang mượn</h2></div><Link to="/my-borrows" className="text-link">Xem tất cả →</Link></section>
            {borrows.loading && !borrows.data && <div className="loading-card">Đang tải dữ liệu...</div>}
            {!borrows.loading && active.length === 0 && <div className="empty-card"><div className="empty-icon">⌁</div><h3>Chưa có sách đang mượn</h3><p>Kho sách đang có rất nhiều điều thú vị chờ bạn khám phá.</p><Link className="button primary" to="/books">Tìm sách ngay</Link></div>}
            <div className="borrow-grid">
                {active.slice(0, 4).map((record) => <article className="borrow-card" key={record.id}><div className="book-cover"><span>LIB</span><b>{record.bookTitle.slice(0, 1)}</b></div><div className="borrow-info"><StatusBadge record={record} /><h3>{record.bookTitle}</h3><p className="muted">Hạn trả <strong>{formatDate(record.dueDate)}</strong></p><div className="card-actions"><button className="button ghost" onClick={() => action(record, 'renew')} disabled={busyId === record.id || record.remainingRenewals === 0}>Gia hạn</button><button className="button primary" onClick={() => action(record, 'return')} disabled={busyId === record.id}>Trả sách</button></div></div></article>)}
            </div>
            {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
        </div>
    );
}
