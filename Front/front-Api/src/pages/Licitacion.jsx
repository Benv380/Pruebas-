import { useEffect, useState } from "react";

function Licitacion() {
    const [codigo, setCodigo] = useState("");
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        document.title = "Consulta Licitación";
    }, []);

    async function handleSubmit(e) {
        e.preventDefault();
        const codigoLimpio = codigo.trim();
        if (!codigoLimpio) return;

        setLoading(true);
        setError(null);
        setData(null);

        try {
            const res = await fetch(`/compra/licitacion/${encodeURIComponent(codigoLimpio)}`);
            if (!res.ok) {
                throw new Error(`El servidor respondió con estado ${res.status}`);
            }
            const json = await res.json();
            setData(json);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="flex-min-w-0">
            <h1>Consulta Licitación</h1>
            <p>Ingresá el código de una Licitación (ej: 1234-5-COT26) para ver su detalle.</p>

            <form className="card-panel d-flex flex-wrap gap-2 mb-4" style={{ maxWidth: "560px" }} onSubmit={handleSubmit}>
                <input
                    type="text"
                    className="form-control flex-grow-1"
                    style={{ minWidth: "220px" }}
                    placeholder="Código de licitación"
                    value={codigo}
                    onChange={(e) => setCodigo(e.target.value)}
                />
                <button type="submit" className="btn btn-primary" disabled={loading}>
                    {loading ? "Buscando..." : "Buscar"}
                </button>
            </form>

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {data && <pre className="data-box">{JSON.stringify(data, null, 2)}</pre>}
        </div>
    );
}

export default Licitacion;
