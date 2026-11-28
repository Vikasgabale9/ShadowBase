
import {
  Users,
  Building2,
  BriefcaseBusiness,
  Activity
} from "lucide-react";

function Dashboard() {
  const stats = [
    {
      title: "Total Employees",
      value: "--",
      description: "Employee records",
      icon: Users,
      color: "blue"
    },
    {
      title: "Departments",
      value: "--",
      description: "Organizational units",
      icon: Building2,
      color: "green"
    },
    {
      title: "Projects",
      value: "--",
      description: "Project records",
      icon: BriefcaseBusiness,
      color: "purple"
    },
    {
      title: "System Status",
      value: "Active",
      description: "Dashboard interface",
      icon: Activity,
      color: "orange"
    }
  ];

  return (
    <div>
      <div className="sb-page-heading">
        <div>
          <h3>Dashboard</h3>
          <p>Welcome to your ShadowBase Management Console.</p>
        </div>
      </div>

      <div className="row g-4 mb-4">
        {stats.map((item) => {
          const Icon = item.icon;

          return (
            <div className="col-12 col-sm-6 col-xl-3" key={item.title}>
              <div className="sb-stat-card">
                <div className="d-flex justify-content-between">
                  <div>
                    <p className="sb-stat-title">{item.title}</p>
                    <h3>{item.value}</h3>
                    <small>{item.description}</small>
                  </div>

                  <div className={`sb-stat-icon ${item.color}`}>
                    <Icon size={23} />
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-8">
          <div className="sb-content-card h-100">
            <h5>Welcome to ShadowBase</h5>
            <p className="text-secondary">
              Manage employees, departments and projects from one
              centralized workspace.
            </p>

            <div className="sb-info-panel">
              <h6>Project Overview</h6>
              <p className="mb-0">
                ShadowBase is being developed as a zero-downtime
                schema migration sandbox.
              </p>
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-4">
          <div className="sb-content-card h-100">
            <h5>Quick Navigation</h5>

            <p className="text-secondary mb-2">
              Use the sidebar to access the available modules.
            </p>

            <ul className="sb-quick-list">
              <li>Employee Management</li>
              <li>Department Management</li>
              <li>Project Management</li>
              <li>Schema Migration – planned</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
