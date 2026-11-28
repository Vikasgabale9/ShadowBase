
import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2, RefreshCw, FolderKanban } from "lucide-react";
import projectService from "../services/projectService";

const initialForm = {
  projectName: "",
  description: "",
  status: "PLANNED",
  startDate: "",
  endDate: "",
};

function ProjectPage() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(initialForm);

  const loadProjects = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await projectService.getAllProjects();
      setProjects(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Failed to load projects."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProjects();
  }, []);

  const resetForm = () => {
    setForm(initialForm);
    setEditingId(null);
    setShowForm(false);
    setError("");
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleEdit = (project) => {
    setForm({
      projectName: project.projectName || "",
      description: project.description || "",
      status: project.status || "PLANNED",
      startDate: project.startDate || "",
      endDate: project.endDate || "",
    });

    setEditingId(project.id);
    setShowForm(true);
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (
      form.startDate &&
      form.endDate &&
      form.endDate < form.startDate
    ) {
      setError("End date cannot be before start date.");
      return;
    }

    const projectData = {
      projectName: form.projectName.trim(),
      description: form.description.trim(),
      status: form.status,
      startDate: form.startDate || null,
      endDate: form.endDate || null,
    };

    try {
      setSaving(true);
      setError("");

      if (editingId) {
        await projectService.updateProject(editingId, projectData);
      } else {
        await projectService.createProject(projectData);
      }

      resetForm();
      await loadProjects();
    } catch (err) {
      setError(
        err.response?.data?.message || "Failed to save project."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this project?")) {
      return;
    }

    try {
      setError("");
      await projectService.deleteProject(id);
      await loadProjects();
    } catch (err) {
      setError(
        err.response?.data?.message || "Failed to delete project."
      );
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case "COMPLETED":
        return "bg-success";
      case "IN_PROGRESS":
        return "bg-primary";
      case "CANCELLED":
        return "bg-danger";
      default:
        return "bg-secondary";
    }
  };

  return (
    <div className="container-fluid py-3">

      <div className="sb-page-heading d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
        <div>
          <h2 className="mb-1">
            <FolderKanban size={26} className="me-2" />
            Project Management
          </h2>
          <p className="text-muted mb-0">
            Create, manage and track projects.
          </p>
        </div>

        <div className="d-flex gap-2">
          <button
            className="btn btn-outline-primary"
            onClick={loadProjects}
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
            Add Project
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {showForm && (
        <div className="sb-content-card p-4 mb-4">
          <h5 className="mb-3">
            {editingId ? "Edit Project" : "Add New Project"}
          </h5>

          <form onSubmit={handleSubmit}>
            <div className="row g-3">

              <div className="col-md-6">
                <label className="form-label">Project Name *</label>
                <input
                  type="text"
                  name="projectName"
                  className="form-control"
                  value={form.projectName}
                  onChange={handleChange}
                  maxLength={150}
                  required
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
                  <option value="PLANNED">Planned</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>

              <div className="col-12">
                <label className="form-label">Description</label>
                <textarea
                  name="description"
                  className="form-control"
                  rows="3"
                  value={form.description}
                  onChange={handleChange}
                  placeholder="Enter project description"
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Start Date</label>
                <input
                  type="date"
                  name="startDate"
                  className="form-control"
                  value={form.startDate}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">End Date</label>
                <input
                  type="date"
                  name="endDate"
                  className="form-control"
                  value={form.endDate}
                  onChange={handleChange}
                  min={form.startDate || undefined}
                />
              </div>

              <div className="col-12 d-flex gap-2">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={saving}
                >
                  {saving
                    ? "Saving..."
                    : editingId
                    ? "Update Project"
                    : "Create Project"}
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

            </div>
          </form>
        </div>
      )}

      <div className="sb-content-card p-4">

        <div className="d-flex justify-content-between align-items-center mb-3">
          <h5 className="mb-0">All Projects</h5>
          <span className="badge bg-primary">
            Total Projects: {projects.length}
          </span>
        </div>

        {loading ? (
          <div className="text-center py-4">
            Loading projects...
          </div>
        ) : projects.length === 0 ? (
          <div className="text-center text-muted py-5">
            <FolderKanban size={40} className="mb-2" />
            <p className="mb-0">No projects found. Add your first project.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Project Name</th>
                  <th>Description</th>
                  <th>Status</th>
                  <th>Start Date</th>
                  <th>End Date</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {projects.map((project) => (
                  <tr key={project.id}>
                    <td>{project.id}</td>
                    <td className="fw-semibold">
                      {project.projectName}
                    </td>
                    <td>{project.description || "-"}</td>
                    <td>
                      <span className={`badge ${getStatusClass(project.status)}`}>
                        {project.status?.replace("_", " ")}
                      </span>
                    </td>
                    <td>{project.startDate || "-"}</td>
                    <td>{project.endDate || "-"}</td>
                    <td>
                      <div className="d-flex gap-2">
                        <button
                          className="btn btn-sm btn-outline-primary"
                          onClick={() => handleEdit(project)}
                          title="Edit Project"
                        >
                          <Pencil size={15} />
                        </button>

                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDelete(project.id)}
                          title="Delete Project"
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

export default ProjectPage;
