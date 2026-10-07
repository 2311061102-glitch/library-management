import type { BorrowRecord } from '../types/borrow';

export default function StatusBadge({ record }: { record: BorrowRecord }) {
    if (record.status === 'RETURNED') return <span className="badge badge-ok">Đã trả</span>;
    if (record.overdue) return <span className="badge badge-danger">Quá hạn {record.overdueDays} ngày</span>;
    return <span className="badge badge-info">Đang mượn</span>;
}
