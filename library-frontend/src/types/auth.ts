export type Role = 'LIBRARIAN' | 'READER';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
    fullName: string;
}

export interface LoginResponse {
    userId: number;
    token: string;
    username: string;
    role: Role;
}
