import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2, RefreshCw, Building2 } from "lucide-react";
import departmentService from "../services/departmentService";

function DepartmentPage() {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [departmentName, setDepartmentName] = useState("");
  const [location, setLocation] = useState("");
  const [saving, setSaving] = useState(false);

  const loadDepartments = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await departmentService.getAllDepartments();
      setDepartments(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load departments.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDepartments();
  }, []);

  const resetForm = () => {
    setDepartmentName("");
    setLocation("");
    setEditingId(null);
    setShowForm(false);
    setError("");
  };

  const handleEdit = (department) => {
    setEditingId(department.id);
    setDepartmentName(department.departmentName || "");
    setLocation(department.location || "");
    setError("");
    setShowForm(true);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!departmentName.trim()) {
      setError("Department name is required.");
      return;
    }

    try {
      setSaving(true);
      setError("");

      const departmentData = {
        departmentName: departmentName.trim(),
        location: location.trim(),
      };

      if (editingId) {
        await departmentService.updateDepartment(editingId, departmentData);
      } else {
        await departmentService.createDepartment(departmentData);
      }

      resetForm();
      await loadDepartments();
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to save department."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this department?")) {
      return;
    }

    try {
      setError("");
      await departmentService.deleteDepartment(id);
      await loadDepartments();
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to delete department."
      );
    }
  };

  return (
    <div className="container-fluid">
      <div className="sb-page-heading d-flex justify-content-between align-items-center flex-wrap gap-3">
        <div>
          <h2>
            <Building2 className="me-2" size={25} />
            Department Management
          </h2>
          <p className="text-secondary mb-0">
            Create, view, update and manage departments.
          </p>
        </div>

        <div className="d-flex gap-2">
          <button
            className="btn btn-outline-secondary"
            onClick={loadDepartments}
            disabled={loading}
          >
            <RefreshCw size={16} className="me-1" />
            Refresh
          </button>

          <button
            className="btn btn-primary"
            onClick={() => {
              resetForm();
              setShowForm(true);
            }}
          >
            <Plus size={17} className="me-1" />
            Add Department
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
            {editingId ? "Edit Department" : "Add Department"}
          </h5>

          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Department Name *</label>
                <input
                  type="text"
                  className="form-control"
                  value={departmentName}
                  onChange={(e) => setDepartmentName(e.target.value)}
                  maxLength={100}
                  placeholder="Enter department name"
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Location</label>
                <input
                  type="text"
                  className="form-control"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  maxLength={150}
                  placeholder="Enter location"
                />
              </div>
            </div>

            <div className="d-flex gap-2 mt-3">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={saving}
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
          <h5 className="mb-0">Departments</h5>
          <span className="text-secondary">
            Total: {departments.length}
          </span>
        </div>

        {loading ? (
          <p className="text-secondary mb-0">Loading departments...</p>
        ) : departments.length === 0 ? (
          <p className="text-secondary mb-0">
            No departments found. Click "Add Department" to create one.
          </p>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Department Name</th>
                  <th>Location</th>
                  <th>Created At</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {departments.map((department) => (
                  <tr key={department.id}>
                    <td>{department.id}</td>
                    <td>{department.departmentName}</td>
                    <td>{department.location || "-"}</td>
                    <td>
                      {department.createdAt
                        ? new Date(department.createdAt).toLocaleString()
                        : "-"}
                    </td>
                    <td>
                      <div className="d-flex gap-2">
                        <button
                          className="btn btn-sm btn-outline-primary"
                          onClick={() => handleEdit(department)}
                          title="Edit"
                        >
                          <Pencil size={15} />
                        </button>

                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDelete(department.id)}
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

export default DepartmentPage;