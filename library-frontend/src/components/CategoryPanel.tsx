import { useState, type FormEvent } from 'react';
import type { Category } from '../types/book';
import { createCategory, updateCategory, deleteCategory } from '../api/bookApi';
import { getErrorMessage } from '../utils/errors';

interface CategoryPanelProps {
    categories: Category[];
    onChanged: () => void; // gọi sau khi thêm/sửa/xóa để trang cha tải lại danh sách
}

export default function CategoryPanel({ categories, onChanged }: CategoryPanelProps) {
    const [editing, setEditing] = useState<Category | null>(null);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [error, setError] = useState<string | null>(null);

    const reset = () => {
        setEditing(null);
        setName('');
        setDescription('');
        setError(null);
    };

    const startEdit = (c: Category) => {
        setEditing(c);
        setName(c.name);
        setDescription(c.description ?? '');
        setError(null);
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!name.trim()) {
            setError('Tên thể loại không được để trống');
            return;
        }
        try {
            if (editing) await updateCategory(editing.id, { name, description });
            else await createCategory({ name, description });
            reset();
            onChanged();
        } catch (err) {
            setError(getErrorMessage(err, 'Lưu thể loại không thành công.'));
        }
    };

    const handleDelete = async (c: Category) => {
        if (!window.confirm(`Xóa thể loại "${c.name}"?`)) return;
        try {
            await deleteCategory(c.id);
            onChanged();
        } catch (err) {
            alert(getErrorMessage(err, 'Xóa thể loại không thành công.'));
        }
    };

    return (
        <section className="card">
            <h3>Thể loại sách</h3>
            <form onSubmit={handleSubmit} className="inline-form">
                <input placeholder="Tên thể loại" value={name} onChange={(e) => setName(e.target.value)} />
                <input placeholder="Mô tả (không bắt buộc)" value={description}
                       onChange={(e) => setDescription(e.target.value)} />
                <button type="submit" className="primary">{editing ? 'Cập nhật' : 'Thêm'}</button>
                {editing && <button type="button" onClick={reset}>Hủy</button>}
            </form>
            {error && <p className="field-error">{error}</p>}

            <div className="table-wrap">
                <table>
                    <thead>
                        <tr><th>Thể loại</th><th>Mô tả</th><th>Số sách</th><th>Thao tác</th></tr>
                    </thead>
                    <tbody>
                        {categories.map((c) => (
                            <tr key={c.id}>
                                <td>{c.name}</td>
                                <td>{c.description ?? '—'}</td>
                                <td>{c.bookCount}</td>
                                <td className="actions">
                                    <button onClick={() => startEdit(c)}>Sửa</button>
                                    <button className="danger" onClick={() => handleDelete(c)}>Xóa</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
