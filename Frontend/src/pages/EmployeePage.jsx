
import { useEffect, useState } from "react";
import {
  Plus,
  Pencil,
  Trash2,
  RefreshCw,
  Users,
} from "lucide-react";
import employeeService from "../services/employeeService";
import departmentService from "../services/departmentService";

function EmployeePage() {
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    salary: "",
    hireDate: "",
    status: "ACTIVE",
    departmentId: "",
  });

  const loadData = async () => {
    try {
      setLoading(true);
      setError("");

      const [employeeData, departmentData] = await Promise.all([
        employeeService.getAllEmployees(),
        departmentService.getAllDepartments(),
      ]);

      setEmployees(Array.isArray(employeeData) ? employeeData : []);
      setDepartments(Array.isArray(departmentData) ? departmentData : []);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to load employees or departments."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const resetForm = () => {
    setForm({
      fullName: "",
      email: "",
      phone: "",
      salary: "",
      hireDate: "",
      status: "ACTIVE",
      departmentId: "",
    });
    setEditingId(null);
    setShowForm(false);
  };

  const handleEdit = (employee) => {
    setForm({
      fullName: employee.fullName || "",
      email: employee.email || "",
      phone: employee.phone || "",
      salary: employee.salary ?? "",
      hireDate: employee.hireDate || "",
      status: employee.status || "ACTIVE",
      departmentId: employee.departmentId?.toString() || "",
    });

    setEditingId(employee.id);
    setError("");
    setShowForm(true);
  };

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.departmentId) {
      setError("Please select a department.");
      return;
    }

    try {
      setSaving(true);
      setError("");

      const employeeData = {
        ...form,
        salary: Number(form.salary),
        departmentId: Number(form.departmentId),
        hireDate: form.hireDate || null,
      };

      if (editingId) {
        await employeeService.updateEmployee(editingId, employeeData);
      } else {
        await employeeService.createEmployee(employeeData);
      }

      resetForm();
      await loadData();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          err.message ||
          "Unable to save employee."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this employee?")) {
      return;
    }

    try {
      setError("");
      await employeeService.deleteEmployee(id);
      await loadData();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to delete employee. Check your permissions."
      );
    }
  };

  return (
    <div className="container-fluid">
      <div className="sb-page-heading d-flex justify-content-between align-items-center flex-wrap gap-3">
        <div>
          <h2>
            <Users className="me-2" size={25} />
            Employee Management
          </h2>
          <p className="text-secondary mb-0">
            Create, view, update and manage employees.
          </p>
        </div>

        <div className="d-flex gap-2">
          <button
            className="btn btn-outline-secondary"
            onClick={loadData}
            disabled={loading}
          >
            <RefreshCw size={16} className="me-1" />
            Refresh
          </button>

          <button
            className="btn btn-primary"
            onClick={() => {
              resetForm();
              setError("");
              setShowForm(true);
            }}
          >
            <Plus size={17} className="me-1" />
            Add Employee
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger mt-3" role="alert">
          {error}
        </div>
      )}

      {showForm && (
        <div className="sb-content-card mt-3">
          <h5 className="mb-3">
            {editingId ? "Edit Employee" : "Add Employee"}
          </h5>

          {departments.length === 0 && (
            <div className="alert alert-warning">
              No departments available. Create a department first.
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Full Name *</label>
                <input
                  type="text"
                  name="fullName"
                  className="form-control"
                  value={form.fullName}
                  onChange={handleChange}
                  maxLength={255}
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Email *</label>
                <input
                  type="email"
                  name="email"
                  className="form-control"
                  value={form.email}
                  onChange={handleChange}
                  maxLength={255}
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Phone</label>
                <input
                  type="text"
                  name="phone"
                  className="form-control"
                  value={form.phone}
                  onChange={handleChange}
                  maxLength={20}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Salary *</label>
                <input
                  type="number"
                  name="salary"
                  className="form-control"
                  value={form.salary}
                  onChange={handleChange}
                  min="0"
                  step="0.01"
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Hire Date</label>
                <input
                  type="date"
                  name="hireDate"
                  className="form-control"
                  value={form.hireDate}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Status *</label>
                <select
                  name="status"
                  className="form-select"
                  value={form.status}
                  onChange={handleChange}
                  required
                >
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                  <option value="ON_LEAVE">On Leave</option>
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label">Department *</label>
                <select
                  name="departmentId"
                  className="form-select"
                  value={form.departmentId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Department</option>
                  {departments.map((department) => (
                    <option key={department.id} value={department.id}>
                      {department.departmentName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="d-flex gap-2 mt-3">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={saving || departments.length === 0}
              >
                {saving ? "Saving..." : editingId ? "Update" : "Save"}
              </button>

              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={resetForm}
                disabled={saving}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="sb-content-card mt-3">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h5 className="mb-0">Employees</h5>
          <span className="text-secondary">
            Total: {employees.length}
          </span>
        </div>

        {loading ? (
          <p className="text-secondary">Loading employees...</p>
        ) : employees.length === 0 ? (
          <p className="text-secondary">
            No employees found. Click "Add Employee" to create one.
          </p>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Full Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Salary</th>
                  <th>Department</th>
                  <th>Status</th>
                  <th>Hire Date</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {employees.map((employee) => (
                  <tr key={employee.id}>
                    <td>{employee.id}</td>
                    <td>{employee.fullName}</td>
                    <td>{employee.email}</td>
                    <td>{employee.phone || "-"}</td>
                    <td>{employee.salary}</td>
                    <td>{employee.departmentName || "-"}</td>
                    <td>{employee.status}</td>
                    <td>{employee.hireDate || "-"}</td>
                    <td>
                      <div className="d-flex gap-2">
                        <button
                          className="btn btn-sm btn-outline-primary"
                          onClick={() => handleEdit(employee)}
                          title="Edit"
                        >
                          <Pencil size={15} />
                        </button>

                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDelete(employee.id)}
                          title="Delete"
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default EmployeePage;
