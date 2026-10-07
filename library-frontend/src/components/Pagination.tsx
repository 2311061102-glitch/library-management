interface PaginationProps {
    currentPage: number; // bắt đầu từ 0, đúng định dạng Spring Data Pageable
    totalPages: number;
    onPageChange: (page: number) => void;
}

export default function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
    if (totalPages <= 1) return null;

    const pages = Array.from({ length: totalPages }, (_, i) => i);

    return (
        <div className="pagination">
            <button disabled={currentPage === 0} onClick={() => onPageChange(currentPage - 1)}>
                « Trang trước
            </button>
            {pages.map((p) => (
                <button
                    key={p}
                    onClick={() => onPageChange(p)}
                    className={p === currentPage ? 'active' : ''}
                >
                    {p + 1}
                </button>
            ))}
            <button disabled={currentPage >= totalPages - 1} onClick={() => onPageChange(currentPage + 1)}>
                Trang sau »
            </button>
        </div>
    );
}
