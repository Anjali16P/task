import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to={isAdmin ? '/admin' : '/feedback'} className="navbar-brand">
          Feedback Manager
        </Link>
        {user && (
          <div className="navbar-user">
            <span className="badge badge-role">{isAdmin ? 'ADMIN' : 'USER'}</span>
            <span className="navbar-username">{user.username}</span>
            <button type="button" className="btn btn-outline btn-sm" onClick={handleLogout}>
              Logout
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
