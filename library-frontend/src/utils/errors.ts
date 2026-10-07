import axios from 'axios';
import type { ApiErrorResponse } from '../types/apiError';

/** Lấy thông báo lỗi thân thiện từ response của backend (message hoặc lỗi validate theo từng field). */
export function getErrorMessage(err: unknown, fallback: string): string {
    if (axios.isAxiosError<ApiErrorResponse>(err)) {
        if (!err.response) return 'Không kết nối được tới hệ thống. Vui lòng thử lại sau.';
        const data = err.response.data;
        if (data?.message) return data.message;
        if (data && typeof data === 'object') {
            const firstFieldError = Object.values(data).find((v) => typeof v === 'string');
            if (firstFieldError) return firstFieldError;
        }
    }
    return fallback;
}
