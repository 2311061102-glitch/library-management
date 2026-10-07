import { useEffect, useState, useCallback } from 'react';
import { getErrorMessage } from '../utils/errors';

interface FetchResult<T> {
    fetcher: () => Promise<T>; // fetcher + key đã sinh ra kết quả này, dùng để suy ra trạng thái "đang tải"
    key: number;
    data: T | null;
    error: string | null;
}

/**
 * Hook tải dữ liệu dùng chung. `fetcher` phải có identity ổn định (module-level hoặc useCallback);
 * khi fetcher đổi (đổi từ khóa, trang, bộ lọc...) hoặc gọi reload() thì tải lại.
 * Trạng thái loading được suy ra từ dữ liệu nên không cần setState đồng bộ trong effect.
 */
export function useFetch<T>(fetcher: () => Promise<T>, fallbackMessage: string) {
    const [reloadKey, setReloadKey] = useState(0);
    const [result, setResult] = useState<FetchResult<T> | null>(null);

    useEffect(() => {
        let cancelled = false;
        fetcher()
            .then((data) => {
                if (!cancelled) setResult({ fetcher, key: reloadKey, data, error: null });
            })
            .catch((err) => {
                if (!cancelled) {
                    setResult((prev) => ({
                        fetcher, key: reloadKey, data: prev?.data ?? null,
                        error: getErrorMessage(err, fallbackMessage),
                    }));
                }
            });
        return () => {
            cancelled = true;
        };
    }, [fetcher, reloadKey, fallbackMessage]);

    const reload = useCallback(() => setReloadKey((k) => k + 1), []);

    const loading = result === null || result.fetcher !== fetcher || result.key !== reloadKey;

    return {
        data: result?.data ?? null, // giữ dữ liệu cũ trong lúc tải lại để bảng không bị nháy
        error: loading ? null : result?.error ?? null,
        loading,
        reload,
    };
}
