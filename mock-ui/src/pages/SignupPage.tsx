import { useState } from 'react';
import api from '../lib/api';
import { setAuthToken } from '../lib/auth';
import { useNavigate, Link } from 'react-router-dom';

export default function SignupPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<'ADMIN' | 'USER'>('USER');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const { data } = await api.post('/api/auth/signup', { username, password, role });
      if (data?.token) {
        setAuthToken(data.token);
        navigate('/');
      } else {
        setError('Invalid response from server');
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Signup failed');
    }
  }

  return (
    <div style={{ maxWidth: 360, margin: '80px auto' }}>
      <h2>Sign up</h2>
      <form onSubmit={onSubmit}>
        <div>
          <label htmlFor="s-username">Username</label>
          <input id="s-username" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
        </div>
        <div>
          <label htmlFor="s-password">Password</label>
          <input id="s-password" placeholder="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </div>
        <div>
          <label htmlFor="s-role">Role</label>
          <select id="s-role" value={role} onChange={(e) => setRole(e.target.value as any)}>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit">Create account</button>
      </form>
      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}


