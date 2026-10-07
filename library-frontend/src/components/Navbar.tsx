import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
    const { user, isAuthenticated, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar">
            <NavLink to="/dashboard" className="brand"><span className="brand-mark">L</span><span>Libra<span className="brand-accent">ry</span></span></NavLink>
            <div className="nav-links">
                {isAuthenticated && <NavLink to="/dashboard">Tổng quan</NavLink>}
                <NavLink to="/books">Kho sách</NavLink>

                {isAuthenticated && user?.role === 'LIBRARIAN' && (
                    <div className="nav-group">
                        <span className="nav-group-label">Quản trị</span>
                        <NavLink to="/admin/books">Sách</NavLink>
                        <NavLink to="/admin/borrows">Mượn trả</NavLink>
                        <NavLink to="/admin/fines">Tiền phạt</NavLink>
                        <NavLink to="/admin/api-keys">API Key</NavLink>
                    </div>
                )}
                {isAuthenticated && user?.role === 'READER' && (
                    <div className="nav-group">
                        <span className="nav-group-label">Cá nhân</span>
                        <NavLink to="/my-borrows">Phiếu mượn</NavLink>
                        <NavLink to="/my-fines">Tiền phạt</NavLink>
                    </div>
                )}
            </div>

            <div className="nav-right">
                {isAuthenticated ? (
                    <>
                        <div className="user-pill"><span className="avatar">{user?.username.slice(0, 1).toUpperCase()}</span><span><strong>{user?.username}</strong><small>{user?.role === 'LIBRARIAN' ? 'Thủ thư' : 'Độc giả'}</small></span></div>
                        <button className="icon-button" onClick={handleLogout} aria-label="Đăng xuất">↪</button>
                    </>
                ) : (
                    <>
                        <NavLink to="/login">Đăng nhập</NavLink>
                        <NavLink to="/register">Đăng ký</NavLink>
                    </>
                )}
            </div>
        </nav>
    );
}
