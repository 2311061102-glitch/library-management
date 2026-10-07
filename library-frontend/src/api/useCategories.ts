import { useState, useEffect, useCallback } from 'react';
import { getCategories } from './bookApi';
import type { Category } from '../types/book';

export function useCategories() {
    const [categories, setCategories] = useState<Category[]>([]);

    const fetchCategories = useCallback(() => {
        getCategories()
            .then((res) => setCategories(res.data))
            .catch(() => setCategories([]));
    }, []);

    useEffect(() => {
        fetchCategories();
    }, [fetchCategories]);

    return { categories, refetchCategories: fetchCategories };
}
