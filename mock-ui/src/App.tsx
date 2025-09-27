import { Outlet, Link, useNavigate } from 'react-router-dom';
import './App.css'
import { clearAuth, getCurrentUser } from './lib/auth';

export default function App() {
  const user = getCurrentUser();
  const navigate = useNavigate();

  function onLogout() {
    clearAuth();
    navigate('/login');
  }

  return (
    <div>
      <header style={{ display: 'flex', gap: 12, alignItems: 'center', padding: 12, borderBottom: '1px solid #eee' }}>
        <strong>Dynamic Mock UI</strong>
        <Link to="/">Dashboard</Link>
        <Link to="/mocks">Mocks</Link>
        <Link to="/tester">API Tester</Link>
        <div style={{ marginLeft: 'auto' }}>
          {user ? (
            <>
              <span style={{ marginRight: 8 }}>{user.username} [{user.roles.join(', ')}]</span>
              <button onClick={onLogout}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <span> · </span>
              <Link to="/signup">Signup</Link>
            </>
          )}
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
