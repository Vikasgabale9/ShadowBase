
import { Github, Database } from "lucide-react";

function Footer() {
  return (
    <footer className="sb-footer">
      <div className="d-flex align-items-center gap-2">
        <Database size={16} />
        <span>ShadowBase</span>
        <span className="text-secondary">|</span>
        <span className="text-secondary">
          Zero-Downtime Schema Migration Sandbox
        </span>
      </div>

      <a
        href="https://github.com/Vikasgabale9/ShadowBase"
        target="_blank"
        rel="noreferrer"
        className="sb-github-link"
      >
        <Github size={17} />
        GitHub
      </a>

      <span className="sb-copyright">
        © {new Date().getFullYear()} ShadowBase
      </span>
    </footer>
  );
}

export default Footer;
