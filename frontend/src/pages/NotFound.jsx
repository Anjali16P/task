import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="auth-page">
      <div className="auth-card card" style={{ textAlign: 'center' }}>
        <h1 className="auth-title">404</h1>
        <p className="auth-subtitle">The page you are looking for does not exist.</p>
        <Link className="btn btn-primary btn-block" to="/">
          Go home
        </Link>
      </div>
    </div>
  );
}
