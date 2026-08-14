import { useEffect, useState } from "react";
import { FilePreviewPanel, descargarBlob, resolverPreview } from "../components/FilePreview";

function Licitacion() {
    const [codigo, setCodigo] = useState("");
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const [licitaciones, setLicitaciones] = useState([]);
    const [expandedLicitaciones, setExpandedLicitaciones] = useState({});
    const [listLoading, setListLoading] = useState(false);
    const [licitacionesArchivos, setLicitacionesArchivos] = useState({}); // { [codigoExterno]: files[] | undefined mientras carga }

    const [archivos, setArchivos] = useState([]);
    const [preview, setPreview] = useState(null); // { modo, url?, blob?, nombre, licitacionCodigo? }
    const [cargandoArchivo, setCargandoArchivo] = useState(null);

    useEffect(() => {
        document.title = "Consulta Licitación";
    }, []);

    useEffect(() => {
        return () => {
            if (preview?.url) URL.revokeObjectURL(preview.url);
        };
    }, [preview]);

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
        setArchivos([]);
        setPreview(null);

        try {
            const res = await fetch(`/compra/licitacion/${encodeURIComponent(codigoLimpio)}`);
            if (!res.ok) {
                throw new Error(`El servidor respondió con estado ${res.status}`);
            }
            const json = await res.json();
            setData(json);

            fetch(`/compra/licitacion/${encodeURIComponent(codigoLimpio)}/adjuntos`)
                .then((r) => (r.ok ? r.json() : null))
                .then((adjuntosJson) => {
                    if (adjuntosJson?.payload?.files) {
                        setArchivos(adjuntosJson.payload.files);
                    }
                })
                .catch((err) => {
                    console.error("Error al pedir adjuntos:", err);
                });
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    // Igual que verArchivo en CompraRapida.jsx: pide el binario del adjunto,
    // decide si hay visor propio (pdf/imagen/docx/xlsx) o si hay que
    // descargarlo directo, y guarda el resultado en `preview`.
    async function verArchivo(id, nombre, licitacionCodigo = null) {
        setCargandoArchivo(id);
        try {
            const res = await fetch(`/compra/licitacion/adjuntos/${id}`);
            if (!res.ok) throw new Error("No se pudo obtener el archivo");

            const blob = await res.blob();
            const preview = resolverPreview(blob, nombre, { licitacionCodigo });
            if (preview) {
                setPreview(preview);
            } else {
                descargarBlob(blob, nombre);
                setPreview(null);
            }
        } catch (err) {
            setError(`Error al abrir "${nombre}": ${err.message}`);
        } finally {
            setCargandoArchivo(null);
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
            const items = json?.Listado || [];
            setLicitaciones(items);
            setLicitacionesArchivos({});

            // Pide los adjuntos de cada licitación en paralelo, sin bloquear el listado.
            items.forEach((item) => {
                fetch(`/compra/licitacion/${encodeURIComponent(item.CodigoExterno)}/adjuntos`)
                    .then((r) => (r.ok ? r.json() : null))
                    .then((adjuntosJson) => {
                        setLicitacionesArchivos((prev) => ({
                            ...prev,
                            [item.CodigoExterno]: adjuntosJson?.payload?.files || [],
                        }));
                    })
                    .catch((err) => {
                        console.error(`Error al pedir adjuntos de ${item.CodigoExterno}:`, err);
                        setLicitacionesArchivos((prev) => ({ ...prev, [item.CodigoExterno]: [] }));
                    });
            });
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

                                    {licitacionesArchivos[item.CodigoExterno] === undefined ? (
                                        <p className="mt-2 mb-0 text-muted" style={{ fontSize: "0.85rem" }}>
                                            Cargando archivos...
                                        </p>
                                    ) : licitacionesArchivos[item.CodigoExterno].length > 0 ? (
                                        <div className="d-flex flex-wrap gap-2 mt-2">
                                            {licitacionesArchivos[item.CodigoExterno].map((f) => (
                                                <button
                                                    key={f.id}
                                                    type="button"
                                                    className="btn btn-outline-secondary btn-sm"
                                                    disabled={cargandoArchivo === f.id}
                                                    onClick={() => verArchivo(f.id, f.nombreArchivo, item.CodigoExterno)}
                                                >
                                                    {cargandoArchivo === f.id ? "Cargando..." : f.nombreArchivo}
                                                </button>
                                            ))}
                                        </div>
                                    ) : (
                                        <p className="mt-2 mb-0 text-muted" style={{ fontSize: "0.85rem" }}>
                                            Sin archivos adjuntos
                                        </p>
                                    )}

                                    {preview?.licitacionCodigo === item.CodigoExterno && (
                                        <FilePreviewPanel preview={preview} onClose={() => setPreview(null)} />
                                    )}

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

            {archivos.length > 0 && (
                <div className="card-panel mb-4">
                    <h5>Documentos adjuntos</h5>
                    <div className="d-flex flex-wrap gap-2">
                        {archivos.map((f) => (
                            <button
                                key={f.id}
                                type="button"
                                className="btn btn-outline-secondary btn-sm"
                                disabled={cargandoArchivo === f.id}
                                onClick={() => verArchivo(f.id, f.nombreArchivo)}
                            >
                                {cargandoArchivo === f.id ? "Cargando..." : f.nombreArchivo}
                            </button>
                        ))}
                    </div>
                </div>
            )}

            {preview && !preview.licitacionCodigo && (
                <FilePreviewPanel preview={preview} onClose={() => setPreview(null)} />
            )}

            {data && <pre className="data-box">{JSON.stringify(data, null, 2)}</pre>}
        </div>
    );
}

export default Licitacion;
