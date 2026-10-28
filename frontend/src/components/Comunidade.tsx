import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, fotoAnuncio } from "../services/anuncios";
interface Loja {
  id: number;
  vendedorId: number;
  nome: string;
  descricao: string;
  endereco: string;
  logoUrl: string;
  whatsapp: string;
}
interface Encontro {
  id: number;
  titulo: string;
  data: string;
  horario: string;
  local: string;
  descricao: string;
  status: string;
}
export function Comunidade({
  tipo,
  compact = false,
}: {
  tipo: "lojas" | "encontros";
  compact?: boolean;
}) {
  const [lojas, setLojas] = useState<Loja[]>([]);
  const [eventos, setEventos] = useState<Encontro[]>([]);
  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  useEffect(() => {
    let ativo = true;
    setLoading(true);
    setErro("");
    (async () => {
      if (tipo === "lojas") {
        const r = await api<{ content: Loja[]; last: boolean }>(
          `/lojas?page=${page}&size=12`,
        );
        if (ativo) {
          setLojas(r.content);
          setLast(r.last);
        }
      } else {
        const r = await api<Encontro[]>("/encontros");
        if (ativo) setEventos(r);
      }
    })()
      .catch((e) => {
        if (ativo) setErro(e.message);
      })
      .finally(() => {
        if (ativo) setLoading(false);
      });
    return () => {
      ativo = false;
    };
  }, [tipo, page]);
  return (
    <section>
      <h1>
        {tipo === "lojas" ? "Brechós parceiros" : "Encontros da comunidade"}
      </h1>
      {loading && <p role="status">Carregando…</p>}
      {erro && <p role="alert">{erro}</p>}
      {!loading && !erro && !lojas.length && !eventos.length && (
        <p>
          {tipo === "lojas"
            ? "Ainda não há brechós cadastrados."
            : "Nenhum encontro confirmado na agenda."}
        </p>
      )}
      {tipo === "lojas"
        ? lojas.map((l) => (
            <article className="form-card" key={l.id}>
              {l.logoUrl && (
                <img
                  width="64"
                  height="64"
                  style={{ objectFit: "contain" }}
                  src={fotoAnuncio(l.logoUrl)}
                  alt=""
                />
              )}
              <h2>{l.nome}</h2>
              <p>{l.descricao || "Brechó cadastrado na comunidade ISAC."}</p>
              <p>{l.endereco || "Endereço a combinar com o brechó"}</p>
              {l.whatsapp && /^\d{10,13}$/.test(l.whatsapp) && (
                <a
                  href={`https://wa.me/${l.whatsapp}`}
                  target="_blank"
                  rel="noreferrer"
                >
                  Conversar com o brechó
                </a>
              )}
              <Link to={`/catalogo?vendedor=${l.vendedorId}`}>
                Ver peças deste brechó
              </Link>
            </article>
          ))
        : eventos.slice(0, compact ? 1 : undefined).map((e) => (
            <article className="form-card" key={e.id}>
              <h2>{e.titulo}</h2>
              <p>
                {e.data.split("-").reverse().join("/")} ·{" "}
                {e.horario.slice(0, 5)}
              </p>
              <p>{e.local}</p>
              <p>{e.descricao}</p>
              <small>{e.status}</small>
            </article>
          ))}
      {tipo === "lojas" && (
        <div className="pagination">
          <button disabled={!page || loading} onClick={() => setPage(page - 1)}>
            Anterior
          </button>
          <button disabled={last || loading} onClick={() => setPage(page + 1)}>
            Próxima
          </button>
        </div>
      )}
    </section>
  );
}
