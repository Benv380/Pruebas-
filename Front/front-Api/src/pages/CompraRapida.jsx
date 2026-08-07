import { useEffect, useRef, useState } from "react";
import { renderAsync } from "docx-preview";
import * as XLSX from "xlsx";

const MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
const MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

// Renderiza un .docx dentro de un iframe (usando docx-preview para convertirlo
// a HTML/CSS en el momento, inyectado directo en el documento del iframe).
function DocxIframeViewer({ blob, title }) {
    const iframeRef = useRef(null);

    useEffect(() => {
        const iframe = iframeRef.current;
        if (!iframe) return;

        let cancelado = false;

        const renderContenido = () => {
            const doc = iframe.contentDocument;
            if (!doc || cancelado) return;
            doc.open();
            doc.write(
                "<!DOCTYPE html><html><head><meta charset='utf-8'><style>body{font-family:sans-serif;padding:20px;margin:0;}</style></head><body></body></html>"
            );
            doc.close();
            renderAsync(blob, doc.body).catch((err) => {
                console.error("Error renderizando docx:", err);
            });
        };

        iframe.onload = renderContenido;
        iframe.src = "about:blank";

        return () => {
            cancelado = true;
        };
    }, [blob]);

    return (
        <iframe
            ref={iframeRef}
            title={title}
            width="100%"
            height="600px"
            style={{ border: "1px solid #ddd" }}
        />
    );
}

// Renderiza la primera hoja de un .xlsx como tabla HTML dentro de un iframe
// (usando SheetJS para convertir la hoja a HTML).
function XlsxIframeViewer({ blob, title }) {
    const [html, setHtml] = useState(null);

    useEffect(() => {
        let cancelado = false;
        blob.arrayBuffer().then((buffer) => {
            if (cancelado) return;
            const workbook = XLSX.read(buffer, { type: "array" });
            const nombreHoja = workbook.SheetNames[0];
            const hoja = workbook.Sheets[nombreHoja];
            const tablaHtml = XLSX.utils.sheet_to_html(hoja);
            setHtml(
                `<!DOCTYPE html><html><head><meta charset="utf-8"><style>
                    body{font-family:sans-serif;padding:12px;margin:0;}
                    table{border-collapse:collapse;width:100%;}
                    td,th{border:1px solid #ccc;padding:4px 8px;font-size:13px;text-align:left;}
                </style></head><body>${tablaHtml}</body></html>`
            );
        });
        return () => {
            cancelado = true;
        };
    }, [blob]);

    if (!html) {
        return <p>Cargando hoja de cálculo...</p>;
    }

    return (
        <iframe
            srcDoc={html}
            title={title}
            width="100%"
            height="600px"
            style={{ border: "1px solid #ddd" }}
        />
    );
}

function CompraRapida() {
    const [codigo, setCodigo] = useState("");
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const [compras, setCompras] = useState([]);
    const [expandedCompras, setExpandedCompras] = useState({});
    const [listLoading, setListLoading] = useState(false);
    const [comprasArchivos, setComprasArchivos] = useState({}); // { [codigo]: files[] | undefined mientras carga }

    const [archivos, setArchivos] = useState([]);
    const [preview, setPreview] = useState(null); // { modo, url?, blob?, nombre }
    const [cargandoArchivo, setCargandoArchivo] = useState(null);

    useEffect(() => {
        document.title = "Consulta Compra Ágil";
    }, []);

    useEffect(() => {
        return () => {
            if (preview?.url) URL.revokeObjectURL(preview.url);
        };
    }, [preview]);

    useEffect(() => {
        const id = setInterval(() => {
            obtenerTodasCompras();
        }, 10 * 60 *1000); // cada 10 minutos

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
            const res = await fetch(`/compra/agil/${encodeURIComponent(codigoLimpio)}`);
            if (!res.ok) {
                throw new Error(`El servidor respondió con estado ${res.status}`);
            }
            const json = await res.json();
            setData(json);

            fetch(`/compra/agil/${encodeURIComponent(codigoLimpio)}/adjuntos`)
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

    async function verArchivo(id, nombre, compraCodigo = null) {
        setCargandoArchivo(id);
        try {
            const res = await fetch(`/compra/agil/adjuntos/${id}`);
            if (!res.ok) throw new Error("No se pudo obtener el archivo");

            const blob = await res.blob();
            const tipo = blob.type;

            if (tipo === "application/pdf") {
                const url = URL.createObjectURL(blob);
                setPreview({ modo: "pdf", url, nombre, compraCodigo });
            } else if (tipo.startsWith("image/")) {
                const url = URL.createObjectURL(blob);
                setPreview({ modo: "imagen", url, nombre, compraCodigo });
            } else if (tipo === MIME_DOCX) {
                setPreview({ modo: "docx", blob, nombre, compraCodigo });
            } else if (tipo === MIME_XLSX) {
                setPreview({ modo: "xlsx", blob, nombre, compraCodigo });
            } else {
                // Cualquier otro formato: se descarga con el nombre real
                const url = URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = nombre;
                a.click();
                URL.revokeObjectURL(url);
                setPreview(null);
            }
        } catch (err) {
            setError(`Error al abrir "${nombre}": ${err.message}`);
        } finally {
            setCargandoArchivo(null);
        }
    }

    async function obtenerTodasCompras() {
        setListLoading(true);
        setError(null);
        try {
            const res = await fetch(`/compra/agil/listar`);
            if (!res.ok) {
                throw new Error(`El servidor respondió con estado ${res.status}`);
            }
            const json = await res.json();
            const items = json?.payload?.items || [];
            setCompras(items);
            setComprasArchivos({});

            // Pide los adjuntos de cada compra en paralelo, sin bloquear el listado.
            items.forEach((item) => {
                fetch(`/compra/agil/${encodeURIComponent(item.codigo)}/adjuntos`)
                    .then((r) => (r.ok ? r.json() : null))
                    .then((adjuntosJson) => {
                        setComprasArchivos((prev) => ({
                            ...prev,
                            [item.codigo]: adjuntosJson?.payload?.files || [],
                        }));
                    })
                    .catch((err) => {
                        console.error(`Error al pedir adjuntos de ${item.codigo}:`, err);
                        setComprasArchivos((prev) => ({ ...prev, [item.codigo]: [] }));
                    });
            });
        } catch (err) {
            setError(err.message);
        } finally {
            setListLoading(false);
        }
    }

    function toggleCompraCard(codigo) {
        setExpandedCompras((prev) => ({
            ...prev,
            [codigo]: !prev[codigo],
        }));
    }

    // Arma el bloque de preview (pdf/imagen/docx/xlsx) para un objeto `preview` dado,
    // con un encabezado común que incluye el botón para cerrarla.
    // Se reusa tanto dentro de cada tarjeta como en la vista de búsqueda individual.
    function renderPreview(p) {
        if (!p) return null;

        let cuerpo = null;
        if (p.modo === "pdf") {
            cuerpo = <iframe src={p.url} width="100%" height="600px" title={p.nombre} style={{ border: "1px solid #ddd" }} />;
        } else if (p.modo === "imagen") {
            cuerpo = <img src={p.url} alt={p.nombre} style={{ maxWidth: "100%" }} />;
        } else if (p.modo === "docx") {
            cuerpo = <DocxIframeViewer blob={p.blob} title={p.nombre} />;
        } else if (p.modo === "xlsx") {
            cuerpo = <XlsxIframeViewer blob={p.blob} title={p.nombre} />;
        } else {
            return null;
        }

        return (
            <div className="card-panel mt-2">
                <div className="d-flex justify-content-between align-items-center mb-2">
                    <h6 className="mb-0">{p.nombre}</h6>
                    <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => setPreview(null)}>
                        Cerrar
                    </button>
                </div>
                {cuerpo}
            </div>
        );
    }

    return (
        <div className="d-flex flex-column align-items-center p-4">
            <h1>Consulta Compra Ágil</h1>

            <p>Ingresá el código de una Compra Ágil (ej: 1234-5-COT26) para ver su detalle.</p>


            <form className="card-panel mb-4" style={{ maxWidth: "560px", width: "100%" }} onSubmit={handleSubmit}>
                <div className="d-flex gap-2 align-items-center" style={{ width: "100%" }}>
                    <input
                        type="text"
                        className="form-control"
                        style={{ minWidth: "220px", flex: 1 }}
                        placeholder="Código de compra ágil"
                        value={codigo}
                        onChange={(e) => setCodigo(e.target.value)}
                    />
                    <button type="submit" className="btn btn-primary" disabled={loading}>
                        {loading ? "Buscando..." : "Buscar"}
                    </button>
                </div>
            </form>

            <div className="d-flex gap-2 mb-3">
                <button
                    type="button"
                    className="btn btn-primary"
                    onClick={obtenerTodasCompras}
                    disabled={listLoading}
                >
                    {listLoading ? "Cargando compras..." : "Mostrar todas las compras"}
                </button>
                <button type="button" className="btn btn-outline-secondary" onClick={() => {}}>
                    Acción extra
                </button>
            </div>

            {compras.length > 0 && (
                <div className="w-100 mb-4">
                    <h5>Compras encontradas</h5>
                    <div className="d-flex flex-column gap-3">
                        {compras.map((item) => (
                            <div key={item.codigo} className="card border mb-2">
                                <div className="card-body p-3">
                                    <div className="d-flex justify-content-between align-items-start gap-3">
                                        <div>
                                            <h6 className="mb-1">{item.codigo} - {item.nombre}</h6>
                                            <p className="mb-1 text-muted">
                                                {item.estado?.glosa || "Sin estado"} · {item.convocatoria?.descripcion || "Sin convocatoria"}
                                            </p>
                                            <p className="mb-0 text-secondary" style={{ fontSize: "0.9rem" }}>
                                                Publicación: {item.fechas?.fecha_publicacion || "-"} · Monto: {item.montos?.monto_disponible_clp ?? item.montos?.monto_disponible ?? "-"}
                                            </p>
                                        </div>
                                        <div className="d-flex gap-2">
                                            <button
                                                type="button"
                                                className="btn btn-sm btn-outline-primary"
                                                onClick={() => toggleCompraCard(item.codigo)}
                                            >
                                                {expandedCompras[item.codigo] ? "Ocultar" : "Ver más"}
                                            </button>
                                        </div>
                                    </div>

                                    {comprasArchivos[item.codigo] === undefined ? (
                                        <p className="mt-2 mb-0 text-muted" style={{ fontSize: "0.85rem" }}>
                                            Cargando archivos...
                                        </p>
                                    ) : comprasArchivos[item.codigo].length > 0 ? (
                                        <div className="d-flex flex-wrap gap-2 mt-2">
                                            {comprasArchivos[item.codigo].map((f) => (
                                                <button
                                                    key={f.id}
                                                    type="button"
                                                    className="btn btn-outline-secondary btn-sm"
                                                    disabled={cargandoArchivo === f.id}
                                                    onClick={() => verArchivo(f.id, f.nombreArchivo, item.codigo)}
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

                                    {preview?.compraCodigo === item.codigo && renderPreview(preview)}

                                    {expandedCompras[item.codigo] && (
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

            {preview && !preview.compraCodigo && renderPreview(preview)}

            {data && <pre className="data-box">{JSON.stringify(data, null, 2)}</pre>}
        </div>
    );
}

export default CompraRapida;