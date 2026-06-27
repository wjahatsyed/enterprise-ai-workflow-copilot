import { NavLink, useNavigate } from 'react-router-dom';
import { clearToken } from '../api/apiClient';

const links = [
  { to: '/', label: 'Dashboard' },
  { to: '/documents', label: 'Documents' },
  { to: '/agents', label: 'Agents' },
  { to: '/chat', label: 'Agent Chat' },
  { to: '/workflows', label: 'Workflows' },
  { to: '/approvals', label: 'Approvals' }
];

export default function Navbar() {
  const navigate = useNavigate();

  function logout() {
    clearToken();
    localStorage.removeItem('currentUser');
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <div className="brandBlock">
        <span className="brandMark">AI</span>
        <div>
          <strong>Workflow Copilot</strong>
          <span>Portfolio MVP</span>
        </div>
      </div>
      <div className="navLinks">
        {links.map((link) => (
          <NavLink key={link.to} to={link.to} end={link.to === '/'}>
            {link.label}
          </NavLink>
        ))}
      </div>
      <button className="secondaryButton" type="button" onClick={logout}>
        Sign out
      </button>
    </nav>
  );
}
