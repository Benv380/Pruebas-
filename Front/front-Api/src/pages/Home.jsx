import { useEffect } from "react";

export default function Home() {
    useEffect(() => {
        document.title = "Home";
    }, []);

    return (
        <div className="card-panel" style={{ maxWidth: "640px" }}>
            <h1>Home</h1>
            <p>Contenido de la página de inicio.</p>
            <img src="/assets/gato.gif" alt="" width={479} />
        </div>
    );
}
