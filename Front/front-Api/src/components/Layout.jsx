import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar.jsx";

export default function Layout() {
    return (
        <div className="d-flex" style={{ minHeight: "100vh" }}>
            <Sidebar />
            {/* flex-min-w-0: sin esto, contenido ancho (ej. el JSON de una
                API) empuja todo el layout hacia el costado en vez de
                scrollear dentro de su propia caja. */}
            <main className="flex-grow-1 flex-min-w-0 p-4">
                <Outlet />
            </main>
        </div>
    );
}
