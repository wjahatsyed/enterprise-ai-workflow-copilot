import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import { useEffect, useState } from 'react';

export default function Layout() {
  const [workspaceId, setWorkspaceId] = useState(
    () => localStorage.getItem('workspaceId') ?? ''
  );
  const user = localStorage.getItem('currentUser');
  const parsedUser = user ? JSON.parse(user) as { email: string; role: string; tenantId: string } : null;

  useEffect(() => {
    if (workspaceId.trim()) {
      localStorage.setItem('workspaceId', workspaceId.trim());
    } else {
      localStorage.removeItem('workspaceId');
    }
  }, [workspaceId]);

  return (
    <div className="appShell">
      <Navbar />
      <main className="mainContent">
        <header className="topBar">
          <div>
            <p className="eyebrow">Workspace context</p>
            <h1>Enterprise AI Workflow Copilot</h1>
          </div>
          <div className="workspacePanel">
            <label htmlFor="workspaceId">Workspace ID</label>
            <input
              id="workspaceId"
              value={workspaceId}
              onChange={(event) => setWorkspaceId(event.target.value)}
              placeholder="Paste workspace UUID"
            />
          </div>
          {parsedUser && (
            <div className="userPill">
              <span>{parsedUser.email}</span>
              <strong>{parsedUser.role}</strong>
            </div>
          )}
        </header>
        <Outlet />
      </main>
    </div>
  );
}
