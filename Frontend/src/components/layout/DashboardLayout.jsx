
import { useState } from "react";
import { Outlet } from "react-router-dom";

import Header from "./Header";
import Sidebar from "./Sidebar";
import Footer from "./Footer";

function DashboardLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleMenuClick = () => {
    if (window.innerWidth <= 768) {
      setMobileOpen((previous) => !previous);
    } else {
      setCollapsed((previous) => !previous);
    }
  };

  const closeMobileSidebar = () => {
    setMobileOpen(false);
  };

  return (
    <div className="sb-app">
      <Header onMenuClick={handleMenuClick} />

      <div className="sb-body">
        <Sidebar
          collapsed={collapsed}
          mobileOpen={mobileOpen}
          onClose={closeMobileSidebar}
        />

        <main
          className={`sb-main ${
            collapsed ? "sb-main-expanded" : ""
          }`}
        >
          <div className="sb-page-content">
            <Outlet />
          </div>

          <Footer />
        </main>
      </div>
    </div>
  );
}

export default DashboardLayout;
