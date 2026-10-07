import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BooksPage from './pages/BooksPage';
import BorrowBookPage from './pages/BorrowBookPage';
import MyBorrowsPage from './pages/MyBorrowsPage';
import AdminBooksPage from './pages/AdminBooksPage';
import AdminBorrowsPage from './pages/AdminBorrowsPage';
import FinesPage from './pages/FinesPage';
import ApiKeysPage from './pages/ApiKeysPage';
import DashboardPage from './pages/DashboardPage';

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Navbar />
                <Routes>
                    <Route path="/" element={<Navigate to="/dashboard" replace />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/books" element={<BooksPage />} />
                    <Route path="/dashboard" element={
                        <ProtectedRoute><DashboardPage /></ProtectedRoute>
                    } />

                    {/* Độc giả */}
                    <Route path="/borrow" element={
                        <ProtectedRoute requiredRole="READER"><BorrowBookPage /></ProtectedRoute>
                    } />
                    <Route path="/my-borrows" element={
                        <ProtectedRoute requiredRole="READER"><MyBorrowsPage /></ProtectedRoute>
                    } />
                    <Route path="/my-fines" element={
                        <ProtectedRoute requiredRole="READER"><FinesPage mode="reader" /></ProtectedRoute>
                    } />

                    {/* Thủ thư */}
                    <Route path="/admin/books" element={
                        <ProtectedRoute requiredRole="LIBRARIAN"><AdminBooksPage /></ProtectedRoute>
                    } />
                    <Route path="/admin/borrows" element={
                        <ProtectedRoute requiredRole="LIBRARIAN"><AdminBorrowsPage /></ProtectedRoute>
                    } />
                    <Route path="/admin/fines" element={
                        <ProtectedRoute requiredRole="LIBRARIAN"><FinesPage mode="librarian" /></ProtectedRoute>
                    } />
                    <Route path="/admin/api-keys" element={
                        <ProtectedRoute requiredRole="LIBRARIAN"><ApiKeysPage /></ProtectedRoute>
                    } />

                    <Route path="*" element={<Navigate to="/books" replace />} />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
