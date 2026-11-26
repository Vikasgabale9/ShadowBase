
import { NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  Users,
  Building2,
  BriefcaseBusiness,
  Database,
  Settings,
  X
} from "lucide-react";

function Sidebar({ collapsed, mobileOpen, onClose }) {
  const menuItems = [
    {
      title: "Dashboard",
      path: "/dashboard",
      icon: LayoutDashboard,
      end: true
    },
    {
      title: "Employees",
      path: "/dashboard/employees",
      icon: Users
    },
    {
      title: "Departments",
      path: "/dashboard/departments",
      icon: Building2
    },
    {
      title: "Projects",
      path: "/dashboard/projects",
      icon: BriefcaseBusiness
    }
  ];

  return (
    <>
      {mobileOpen && (
        <div
          className="sb-sidebar-overlay"
          onClick={onClose}
        />
      )}

      <aside
        className={`sb-sidebar
          ${collapsed ? "sb-sidebar-collapsed" : ""}
          ${mobileOpen ? "sb-sidebar-mobile-open" : ""}`}
      >
        <div className="sb-sidebar-top">
          <span className="sb-menu-heading">MAIN MENU</span>

          <button
            className="sb-mobile-close"
            onClick={onClose}
            aria-label="Close sidebar"
          >
            <X size={20} />
          </button>
        </div>

        <nav className="sb-navigation">
          {menuItems.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.end}
                onClick={onClose}
                title={collapsed ? item.title : ""}
                className={({ isActive }) =>
                  `sb-nav-link ${isActive ? "active" : ""}`
                }
              >
                <Icon size={20} />
                <span>{item.title}</span>
              </NavLink>
            );
          })}
        </nav>

        <div className="sb-sidebar-divider" />

        <span className="sb-menu-heading">SYSTEM</span>

        <div className="sb-navigation">
          <div
            className="sb-nav-link sb-nav-disabled"
            title="Planned feature"
          >
            <Database size={20} />
            <span>Schema Migrations</span>
            <small className="sb-coming-soon">Soon</small>
          </div>

          <div
            className="sb-nav-link sb-nav-disabled"
            title="Settings coming soon"
          >
            <Settings size={20} />
            <span>Settings</span>
          </div>
        </div>

        <div className="sb-sidebar-bottom">
          <div className="sb-sidebar-version">
            <span className="sb-status-dot" />
            <span>Development Environment</span>
          </div>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
