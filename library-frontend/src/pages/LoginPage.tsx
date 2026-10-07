import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';

export default function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setSubmitting(true);
        try {
            const res = await loginApi({ username, password });
            login(res.data);
            navigate(res.data.role === 'LIBRARIAN' ? '/admin/books' : '/books');
        } catch (err) {
            setError(getErrorMessage(err, 'Đăng nhập thất bại, vui lòng thử lại.'));
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="auth-card card">
            <h2>Đăng nhập thư viện</h2>
            <form onSubmit={handleSubmit}>
                <label>Tên đăng nhập
                    <input value={username} onChange={(e) => setUsername(e.target.value)} />
                </label>
                <label>Mật khẩu
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                </label>
                {error && <p className="field-error">{error}</p>}
                <button type="submit" className="primary block" disabled={submitting}>
                    {submitting ? 'Đang xử lý...' : 'Đăng nhập'}
                </button>
            </form>
            <p className="muted small">Chưa có tài khoản? <Link to="/register">Đăng ký độc giả</Link></p>
        </div>
    );
}
