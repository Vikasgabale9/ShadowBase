
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Menu,
  Bell,
  UserCircle,
  LogOut,
  ChevronDown
} from "lucide-react";

function Header({ onMenuClick }) {
  const navigate = useNavigate();
  const [profileOpen, setProfileOpen] = useState(false);

  const user = JSON.parse(localStorage.getItem("user") || "{}");

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/", { replace: true });
  };

  return (
    <header className="sb-header">
      <div className="d-flex align-items-center gap-3">
        <button
          className="sb-icon-button"
          onClick={onMenuClick}
          aria-label="Toggle sidebar"
        >
          <Menu size={22} />
        </button>

        <div className="sb-brand d-flex align-items-center gap-2">
          <div className="sb-brand-icon">S</div>
          <div>
            <h5 className="mb-0 fw-bold">ShadowBase</h5>
            <small className="text-secondary">Management Console</small>
          </div>
        </div>
      </div>

      <div className="d-flex align-items-center gap-3">
        <button className="sb-icon-button" title="Notifications">
          <Bell size={20} />
        </button>

        <div className="position-relative">
          <button
            className="sb-profile-button"
            onClick={() => setProfileOpen(!profileOpen)}
          >
            <UserCircle size={25} />
            <span className="d-none d-sm-inline">
              {user.username || "User"}
            </span>
            <ChevronDown size={16} />
          </button>

          {profileOpen && (
            <div className="sb-profile-menu">
              <div className="px-3 py-2 border-bottom">
                <strong>{user.username || "User"}</strong>
                <small className="d-block text-secondary">
                  {user.email || ""}
                </small>
                <small className="d-block text-secondary">
                  {user.role || "EMPLOYEE"}
                </small>
              </div>

              <button
                className="sb-logout-button"
                onClick={handleLogout}
              >
                <LogOut size={17} />
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
