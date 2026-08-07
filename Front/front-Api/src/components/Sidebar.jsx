import { useState } from "react";
import { NavLink } from "react-router-dom";
// import style from "./assets/style.gif";

const links = [
    { to: "/", icon: "bi-house-door-fill", label: "Home" },
    { to: "/compra-agil", icon: "bi-speedometer2", label: "Consulta Compra-Rapida" },
    { to: "/licitacion", icon: "bi-file-earmark-text", label: "Consulta Licitación" },
    { to: "#", icon: "bi-table", label: "" },
    { to: "#", icon: "bi-grid", label: "Products" },
    { to: "#", icon: "bi-people-circle", label: "Customers" },
];

export default function Sidebar() {
    const [collapsed, setCollapsed] = useState(false);

    return (
        <div
            className="d-flex flex-column flex-shrink-0 p-3 text-white"
            style={{
                width: collapsed ? "72px" : "280px",
                minHeight: "100vh",
                background: "var(--bg-elevated)",
                borderRight: "1px solid var(--border)",
                transition: "width 0.2s ease",
                overflow: "hidden",
                contentAlign: "center",
            }}
        >
            <div className="d-flex align-items-center mb-3">
                {!collapsed && (
                    <a href="/" className="d-flex align-items-center flex-grow-1 text-white text-decoration-none text-truncate">
                        <i className="bi bi-bootstrap-fill me-2 fs-4"></i>
                        <span className="fs-5">ZonaTI</span>
                    </a>
                )}
                <button
                    type="button"
                    className="btn btn-sm text-white ms-auto"
                    style={{ background: "transparent", border: "1px solid var(--border)" }}
                    onClick={() => setCollapsed((c) => !c)}
                    title={collapsed ? "Expandir menú" : "Retraer menú"}
                >
                    <i className={`bi ${collapsed ? "bi-chevron-double-right" : "bi-chevron-double-left"}`}></i>
                </button>
            </div>
            <hr className="mt-0" style={{ borderColor: "var(--border)" }} />
            <ul className="nav nav-pills flex-column mb-auto">
                {links.map((link) => (
                    <li key={link.label} className="nav-item">
                        <NavLink
                            to={link.to}
                            title={collapsed ? link.label : undefined}
                            className={({ isActive }) =>
                                `nav-link d-flex align-items-center text-truncate ${isActive && link.to !== "#" ? "active" : "text-white"}`
                            }
                        >
                            <i className={`bi ${link.icon} ${collapsed ? "" : "me-2"}`}></i>
                            {!collapsed && link.label}
                        </NavLink>
                    </li>
                ))}
            </ul>
            <hr style={{ borderColor: "var(--border)" }} />
            <div className="dropdown">
                <a
                    href="#"
                    className="d-flex align-items-center text-white text-decoration-none dropdown-toggle"
                    id="dropdownUser1"
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                >
                    <img src="assets/style.gif" alt="" width="32" height="32" className="rounded-circle me-2 flex-shrink-0" />
                    {!collapsed && <strong className="text-truncate">mdo</strong>}
                </a>
                <ul className="dropdown-menu dropdown-menu-dark text-small shadow" aria-labelledby="dropdownUser1">
                    <li><a className="dropdown-item" href="#">New project...</a></li>
                    <li><a className="dropdown-item" href="#">Settings</a></li>
                    <li><a className="dropdown-item" href="#">Profile</a></li>
                    <li><hr className="dropdown-divider" /></li>
                    <li><a className="dropdown-item" href="#">Sign out</a></li>
                </ul>
            </div>
        </div>
    );
}
