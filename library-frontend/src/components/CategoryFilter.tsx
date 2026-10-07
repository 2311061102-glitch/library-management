import type { Category } from '../types/book';

interface CategoryFilterProps {
    categories: Category[];
    value: number | undefined;
    onChange: (categoryId: number | undefined) => void;
}

export default function CategoryFilter({ categories, value, onChange }: CategoryFilterProps) {
    return (
        <select
            value={value ?? ''}
            onChange={(e) => onChange(e.target.value ? Number(e.target.value) : undefined)}
            aria-label="Lọc theo thể loại"
        >
            <option value="">Tất cả thể loại</option>
            {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
            ))}
        </select>
    );
}
