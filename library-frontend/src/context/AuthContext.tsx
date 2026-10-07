import { createContext, useContext, useState, type ReactNode } from 'react';
import type { LoginResponse, Role } from '../types/auth';
import { TOKEN_KEY, USER_KEY } from '../api/axiosClient';

interface AuthUser {
    id: number;
    username: string;
    role: Role;
}

interface AuthContextValue {
    user: AuthUser | null;
    login: (data: LoginResponse) => void;
    logout: () => void;
    isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadSavedUser(): AuthUser | null {
    const savedUser = localStorage.getItem(USER_KEY);
    const savedToken = localStorage.getItem(TOKEN_KEY);
    if (!savedUser || !savedToken) return null;
    try {
        return JSON.parse(savedUser) as AuthUser;
    } catch {
        return null;
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(loadSavedUser);

    const login = (data: LoginResponse) => {
        localStorage.setItem(TOKEN_KEY, data.token);
        const authUser: AuthUser = { id: data.userId, username: data.username, role: data.role };
        localStorage.setItem(USER_KEY, JSON.stringify(authUser));
        setUser(authUser);
    };

    const logout = () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
            {children}
        </AuthContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth phải được dùng bên trong AuthProvider');
    return ctx;
}
