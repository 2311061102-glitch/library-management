export const formatVnd = (amount: number): string =>
    `${new Intl.NumberFormat('vi-VN').format(amount)} ₫`;

export const formatDate = (iso: string | null | undefined): string =>
    iso ? new Date(iso).toLocaleDateString('vi-VN') : '—';

export const formatDateTime = (iso: string | null | undefined): string =>
    iso ? new Date(iso).toLocaleString('vi-VN') : '—';
