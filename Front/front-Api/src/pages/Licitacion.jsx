import { useEffect, useState } from "react";

function Licitacion() {
    const [codigo, setCodigo] = useState("");
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const [licitaciones, setLicitaciones] = useState([]);
    const [expandedLicitaciones, setExpandedLicitaciones] = useState({});
    const [listLoading, setListLoading] = useState(false);

    useEffect(() => {
        document.title = "Consulta Licitación";
    }, []);

    // Igual que en Compra Ágil: refresca sola la lista de "últimas 8 horas"
    // cada 10 minutos, sin que el usuario tenga que volver a apretar el botón.
    useEffect(() => {
        const id = setInterval(() => {
            obtenerTodasLicitaciones();
        }, 10 * 60 * 1000); // cada 10 minutos

        return () => clearInterval(id);
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

    async function obtenerTodasLicitaciones() {
        setListLoading(true);
        setError(null);
        try {
            const res = await fetch(`/compra/licitacion/listar`);
            if (!res.ok) {
                throw new Error(`El servidor respondió con estado ${res.status}`);
            }
            const json = await res.json();
            setLicitaciones(json?.Listado || []);
        } catch (err) {
            setError(err.message);
        } finally {
            setListLoading(false);
        }
    }

    function toggleLicitacionCard(codigoExterno) {
        setExpandedLicitaciones((prev) => ({
            ...prev,
            [codigoExterno]: !prev[codigoExterno],
        }));
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

            <div className="d-flex gap-2 mb-3">
                <button
                    type="button"
                    className="btn btn-primary"
                    onClick={obtenerTodasLicitaciones}
                    disabled={listLoading}
                >
                    {listLoading ? "Cargando licitaciones..." : "Mostrar licitaciones de las últimas 8 horas"}
                </button>
            </div>

            {licitaciones.length > 0 && (
                <div className="w-100 mb-4">
                    <h5>Licitaciones encontradas</h5>
                    <div className="d-flex flex-column gap-3">
                        {licitaciones.map((item) => (
                            <div key={item.CodigoExterno} className="card border mb-2">
                                <div className="card-body p-3">
                                    <div className="d-flex justify-content-between align-items-start gap-3">
                                        <div>
                                            <h6 className="mb-1">{item.CodigoExterno} - {item.Nombre}</h6>
                                            <p className="mb-1 text-muted">
                                                {item.Estado || "Sin estado"} · {item.Comprador?.NombreOrganismo || "Sin organismo"}
                                            </p>
                                            <p className="mb-0 text-secondary" style={{ fontSize: "0.9rem" }}>
                                                Publicación: {item.Fechas?.FechaPublicacion || "-"} · Cierre: {item.Fechas?.FechaCierre || "-"} · Monto: {item.MontoEstimado ?? "-"} {item.Moneda || ""}
                                            </p>
                                        </div>
                                        <div className="d-flex gap-2">
                                            <button
                                                type="button"
                                                className="btn btn-sm btn-outline-primary"
                                                onClick={() => toggleLicitacionCard(item.CodigoExterno)}
                                            >
                                                {expandedLicitaciones[item.CodigoExterno] ? "Ocultar" : "Ver más"}
                                            </button>
                                        </div>
                                    </div>

                                    {expandedLicitaciones[item.CodigoExterno] && (
                                        <div className="mt-3 border-top pt-3">
                                            <pre className="mb-0" style={{ whiteSpace: "pre-wrap", wordBreak: "break-word", fontSize: "0.9rem" }}>
                                                {JSON.stringify(item, null, 2)}
                                            </pre>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

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
