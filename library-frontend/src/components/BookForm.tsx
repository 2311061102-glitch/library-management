import { useState, type FormEvent } from 'react';
import type { Book, BookFormValues, Category } from '../types/book';
import { emptyBookForm } from '../types/book';

interface BookFormProps {
    editingBook: Book | null; // null = đang ở chế độ Thêm; có giá trị = đang Sửa
    categories: Category[];
    onSubmit: (values: BookFormValues) => Promise<void>;
    onCancel: () => void;
    submitting: boolean;
    serverError: string | null;
}

export default function BookForm({
    editingBook, categories, onSubmit, onCancel, submitting, serverError,
}: BookFormProps) {
    // State khởi tạo từ editingBook. Trang cha đổi `key` để remount form khi đổi sách đang sửa / sau khi lưu.
    const [values, setValues] = useState<BookFormValues>(() => editingBook ? {
        title: editingBook.title,
        author: editingBook.author,
        isbn: editingBook.isbn,
        publishYear: editingBook.publishYear ? String(editingBook.publishYear) : '',
        categoryId: String(editingBook.categoryId),
        totalCopies: String(editingBook.totalCopies),
    } : emptyBookForm);
    const [errors, setErrors] = useState<Partial<Record<keyof BookFormValues, string>>>({});

    const set = (field: keyof BookFormValues, value: string) => setValues((v) => ({ ...v, [field]: value }));

    const validate = (): boolean => {
        const e: Partial<Record<keyof BookFormValues, string>> = {};
        if (!values.title.trim()) e.title = 'Tên sách không được để trống';
        if (!values.author.trim()) e.author = 'Tác giả không được để trống';
        if (!values.isbn.trim()) e.isbn = 'Mã ISBN không được để trống';
        if (!values.categoryId) e.categoryId = 'Vui lòng chọn thể loại';
        const copies = Number(values.totalCopies);
        if (!values.totalCopies || isNaN(copies) || copies <= 0) e.totalCopies = 'Tổng số bản phải là số lớn hơn 0';
        if (values.publishYear) {
            const y = Number(values.publishYear);
            if (isNaN(y) || y < 1000 || y > new Date().getFullYear()) e.publishYear = 'Năm xuất bản không hợp lệ';
        }
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleSubmit = async (ev: FormEvent) => {
        ev.preventDefault();
        if (!validate()) return;
        await onSubmit(values);
    };

    return (
        <form onSubmit={handleSubmit} className="card form-grid">
            <h3>{editingBook ? 'Sửa thông tin sách' : 'Thêm sách mới'}</h3>

            <label>Tên sách
                <input value={values.title} onChange={(e) => set('title', e.target.value)} />
                {errors.title && <span className="field-error">{errors.title}</span>}
            </label>
            <label>Tác giả
                <input value={values.author} onChange={(e) => set('author', e.target.value)} />
                {errors.author && <span className="field-error">{errors.author}</span>}
            </label>
            <label>Mã ISBN
                <input value={values.isbn} onChange={(e) => set('isbn', e.target.value)} />
                {errors.isbn && <span className="field-error">{errors.isbn}</span>}
            </label>
            <label>Thể loại
                <select value={values.categoryId} onChange={(e) => set('categoryId', e.target.value)}>
                    <option value="">-- Chọn thể loại --</option>
                    {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
                {errors.categoryId && <span className="field-error">{errors.categoryId}</span>}
            </label>
            <label>Năm xuất bản
                <input type="number" value={values.publishYear} onChange={(e) => set('publishYear', e.target.value)} />
                {errors.publishYear && <span className="field-error">{errors.publishYear}</span>}
            </label>
            <label>Tổng số bản
                <input type="number" value={values.totalCopies} onChange={(e) => set('totalCopies', e.target.value)} />
                {errors.totalCopies && <span className="field-error">{errors.totalCopies}</span>}
            </label>

            {serverError && <p className="field-error full">{serverError}</p>}

            <div className="full actions">
                <button type="submit" className="primary" disabled={submitting}>
                    {submitting ? 'Đang lưu...' : (editingBook ? 'Cập nhật' : 'Thêm mới')}
                </button>
                {editingBook && <button type="button" onClick={onCancel}>Hủy</button>}
            </div>
        </form>
    );
}
