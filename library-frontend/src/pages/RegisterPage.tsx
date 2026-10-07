import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';

export default function RegisterPage() {
    const [fullName, setFullName] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!fullName.trim() || username.trim().length < 3 || password.length < 6) {
            setError('Vui lòng nhập họ tên, username (≥ 3 ký tự) và mật khẩu (≥ 6 ký tự).');
            return;
        }
        setSubmitting(true);
        try {
            const res = await registerApi({ fullName, username: username.trim(), password });
            login(res.data); // đăng ký xong tự đăng nhập
            navigate('/books');
        } catch (err) {
            setError(getErrorMessage(err, 'Đăng ký thất bại, vui lòng thử lại.'));
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="auth-card card">
            <h2>Đăng ký độc giả</h2>
            <form onSubmit={handleSubmit}>
                <label>Họ và tên
                    <input value={fullName} onChange={(e) => setFullName(e.target.value)} />
                </label>
                <label>Tên đăng nhập
                    <input value={username} onChange={(e) => setUsername(e.target.value)} />
                </label>
                <label>Mật khẩu
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                </label>
                {error && <p className="field-error">{error}</p>}
                <button type="submit" className="primary block" disabled={submitting}>
                    {submitting ? 'Đang xử lý...' : 'Tạo tài khoản'}
                </button>
            </form>
            <p className="muted small">Đã có tài khoản? <Link to="/login">Đăng nhập</Link></p>
        </div>
    );
}
