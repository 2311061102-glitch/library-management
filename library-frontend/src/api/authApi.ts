import axiosClient from './axiosClient';
import type { LoginRequest, LoginResponse, RegisterRequest } from '../types/auth';

export const login = (payload: LoginRequest) =>
    axiosClient.post<LoginResponse>('/api/auth/login', payload);

export const register = (payload: RegisterRequest) =>
    axiosClient.post<LoginResponse>('/api/auth/register', payload);
