import { jwtDecode } from 'jwt-decode';

export type CurrentUser = {
  username: string;
  roles: string[];
} | null;

const TOKEN_KEY = 'token';

export function setAuthToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getCurrentUser(): CurrentUser {
  const token = getAuthToken();
  if (!token) return null;
  try {
    const payload: any = jwtDecode(token);
    const roles = Array.isArray(payload.roles)
      ? payload.roles
      : [payload.role].filter(Boolean);
    const username = payload.sub ?? payload.username ?? 'user';
    return { username, roles: roles ?? [] };
  } catch {
    return null;
  }
}

export function hasRole(required: string | string[]): boolean {
  const user = getCurrentUser();
  if (!user) return false;
  const requiredRoles = Array.isArray(required) ? required : [required];
  return requiredRoles.some((r) => user.roles.includes(r));
}

