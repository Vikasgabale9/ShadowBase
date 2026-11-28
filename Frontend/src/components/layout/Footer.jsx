
import React from "react";

function Footer() {
  return (
    <footer className="sb-footer">
      <div>
        <span>© {new Date().getFullYear()} ShadowBase.</span>
        <span> All rights reserved.</span>
      </div>

      <div>
        <a
          href="https://github.com/Vikasgabale9/ShadowBase"
          target="_blank"
          rel="noopener noreferrer"
        >
          View on GitHub
        </a>
      </div>
    </footer>
  );
}

export default Footer;
