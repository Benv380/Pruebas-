import { useEffect, useRef, useState } from "react";
import { renderAsync } from "docx-preview";
import * as XLSX from "xlsx";

// Compartido entre CompraRapida.jsx y Licitacion.jsx: ambas listan adjuntos
// desde el backend y necesitan la misma lógica de previsualización
// (pdf/imagen inline, docx/xlsx convertidos en el momento, o descarga directa
// para cualquier otro formato).

export const MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
export const MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

// Renderiza un .docx dentro de un iframe (usando docx-preview para convertirlo
// a HTML/CSS en el momento, inyectado directo en el documento del iframe).
export function DocxIframeViewer({ blob, title }) {
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
export function XlsxIframeViewer({ blob, title }) {
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

// Dado un blob ya descargado, decide en qué "modo" debe mostrarse (pdf,
// imagen, docx, xlsx) devolviendo el objeto `preview` listo para guardar en
// estado. Si el formato no tiene visor propio, devuelve null: el llamador
// debe descargarlo directo en vez de previsualizarlo.
export function resolverPreview(blob, nombre, extra = {}) {
    const tipo = blob.type;
    if (tipo === "application/pdf") {
        return { modo: "pdf", url: URL.createObjectURL(blob), nombre, ...extra };
    }
    if (tipo.startsWith("image/")) {
        return { modo: "imagen", url: URL.createObjectURL(blob), nombre, ...extra };
    }
    if (tipo === MIME_DOCX) {
        return { modo: "docx", blob, nombre, ...extra };
    }
    if (tipo === MIME_XLSX) {
        return { modo: "xlsx", blob, nombre, ...extra };
    }
    return null;
}

// Descarga directa de un blob con su nombre real (fallback para formatos sin
// visor propio, ej: .zip, .rar, .txt).
export function descargarBlob(blob, nombre) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = nombre;
    a.click();
    URL.revokeObjectURL(url);
}

// Arma el bloque de preview (pdf/imagen/docx/xlsx) para un objeto `preview`
// dado, con un encabezado común que incluye el botón para cerrarla.
export function FilePreviewPanel({ preview, onClose }) {
    if (!preview) return null;

    let cuerpo = null;
    if (preview.modo === "pdf") {
        cuerpo = <iframe src={preview.url} width="100%" height="600px" title={preview.nombre} style={{ border: "1px solid #ddd" }} />;
    } else if (preview.modo === "imagen") {
        cuerpo = <img src={preview.url} alt={preview.nombre} style={{ maxWidth: "100%" }} />;
    } else if (preview.modo === "docx") {
        cuerpo = <DocxIframeViewer blob={preview.blob} title={preview.nombre} />;
    } else if (preview.modo === "xlsx") {
        cuerpo = <XlsxIframeViewer blob={preview.blob} title={preview.nombre} />;
    } else {
        return null;
    }

    return (
        <div className="card-panel mt-2">
            <div className="d-flex justify-content-between align-items-center mb-2">
                <h6 className="mb-0">{preview.nombre}</h6>
                <button type="button" className="btn btn-sm btn-outline-secondary" onClick={onClose}>
                    Cerrar
                </button>
            </div>
            {cuerpo}
        </div>
    );
}
