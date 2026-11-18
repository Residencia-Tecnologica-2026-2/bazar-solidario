import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/anuncios';
import { obterSessao } from '../services/usuarios';

const base = '/integracoes/mercadolivre';
type Situacao = { configurado: boolean; conectado: boolean; somenteTeste: boolean; apelido?: string };
type Item = { id: string; title: string; price: number; currency_id: string; available_quantity: number; status: string };
type Frete = { options?: { id: number; name: string; cost: number; currency_id?: string; estimated_delivery_time?: { date?: string } }[] };
type Vinculo = { anuncioId: number; itemId: string; statusExterno: string; estoqueExterno: number; consultadoEm: string };
type Atributo = {id: string; name: string; tags?: {required?: boolean; read_only?: boolean}; values?: {id: string; name: string}[]};

export function Integracoes() {
  const [situacao, setSituacao] = useState<Situacao | null>(null);
  const [autorizacao, setAutorizacao] = useState('');
  const [itens, setItens] = useState<string[]>([]);
  const [itemId, setItemId] = useState('');
  const [item, setItem] = useState<Item | null>(null);
  const [cep, setCep] = useState('');
  const [frete, setFrete] = useState<Frete | null>(null);
  const [anuncioId, setAnuncioId] = useState('');
  const [vinculos, setVinculos] = useState<Vinculo[]>([]);
  const [categoria, setCategoria] = useState('');
  const [atributos, setAtributos] = useState<Atributo[]>([]);
  const [valores, setValores] = useState<Record<string,string>>({});
  const [tipoAnuncio, setTipoAnuncio] = useState('free');
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const loja = obterSessao()?.usuario.role === 'LOJA';

  async function executar(acao: () => Promise<void>) {
    setOcupado(true); setErro(''); setMensagem('');
    try { await acao(); } catch (error) { setErro(error instanceof Error ? error.message : 'Não foi possível consultar a integração.'); }
    finally { setOcupado(false); }
  }
  async function atualizar() {
    const status = await api<Situacao>(`${base}/situacao`, {}, true);
    setSituacao(status);
    if (status.conectado) {
      const lista = await api<{ results: string[] }>(`${base}/itens`, {}, true);
      setItens(lista.results);
      setVinculos(await api<Vinculo[]>(`${base}/vinculos`, {}, true));
    }
  }
  useEffect(() => { if (loja) void executar(atualizar); }, [loja]);

  if (!loja) return <div className="info-box"><p>Entre como brechó para gerenciar as integrações.</p><Link to="/login">Fazer login</Link></div>;
  return <>
    <div className="page-heading"><p className="eyebrow">SEU BRECHÓ / CANAIS DE VENDA</p><h1>Mercado Livre</h1><p>Conecte uma conta de teste, confira seus anúncios e consulte opções de envio.</p></div>
    <section className="form-card" aria-busy={ocupado}>
      <h2>Conta conectada</h2>
      <p>Esta versão aceita somente contas de teste do Mercado Livre Brasil.</p>
      {situacao?.conectado ? <p role="status">Conectado como <strong>{situacao.apelido}</strong>.</p> : <p>{situacao?.configurado === false ? 'A equipe precisa configurar a aplicação no servidor antes da conexão.' : 'Sua loja ainda não está conectada.'}</p>}
      <button className="btn full" disabled={ocupado || !situacao?.configurado} onClick={() => void executar(async () => {
        const resposta = await api<{ url: string }>(`${base}/conectar`, { method: 'POST' }, true);
        setAutorizacao(resposta.url);
      })}>Preparar conexão de teste</button>
      {autorizacao && <a className="btn full" href={autorizacao} target="_blank" rel="noopener noreferrer">Autorizar no Mercado Livre</a>}
      <button className="text-button" disabled={ocupado} onClick={() => void executar(atualizar)}>Atualizar conexão</button>
    </section>
    {situacao?.conectado && <>
      <section className="form-card"><h2>Publicar uma peça de teste</h2>
        <p>Escolha uma peça oculta no ISAC, com foto e estoque. O anúncio será identificado como teste e a peça continuará indisponível para compra neste aplicativo.</p>
        <label>Número da peça no ISAC<input inputMode="numeric" value={anuncioId} onChange={e => setAnuncioId(e.target.value.replace(/\D/g, ''))} /></label>
        <label>Categoria do Mercado Livre<input value={categoria} onChange={e => {setCategoria(e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, ''));setAtributos([]);setValores({});}} placeholder="Código MLB da categoria" /></label>
        <button className="text-button" disabled={ocupado || !/^MLB\d{2,20}$/.test(categoria)} onClick={() => void executar(async () => {
          setAtributos(await api<Atributo[]>(`${base}/categorias/${categoria}/atributos`, {}, true));
        })}>Carregar características da categoria</button>
        {atributos.filter(a => !a.tags?.read_only).map(a => <label key={a.id}>{a.name}{a.tags?.required ? ' *' : ''}
          {a.values?.length ? <select value={valores[a.id] || ''} onChange={e => setValores({...valores,[a.id]:e.target.value})}><option value="">Selecione</option>{a.values.map(v => <option key={v.id} value={v.id}>{v.name}</option>)}</select> : <input value={valores[a.id] || ''} maxLength={250} onChange={e => setValores({...valores,[a.id]:e.target.value})} />}
        </label>)}
        <label>Tipo do anúncio de teste<select value={tipoAnuncio} onChange={e => setTipoAnuncio(e.target.value)}><option value="free">Gratuito</option><option value="gold_special">Clássico — somente conta de teste</option></select></label>
        <button className="btn full" disabled={ocupado || !anuncioId || !atributos.length} onClick={() => void executar(async () => {
          const p = await api<{estado:string;aviso:string;itemId?:string}>(`${base}/publicacoes`, {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({anuncioId:Number(anuncioId),categoriaId:categoria,tipoAnuncio,atributos:valores})}, true);
          setMensagem(p.aviso);if(p.itemId)setItemId(p.itemId);await atualizar();setMensagem(p.aviso);
        })}>Validar e publicar anúncio de teste</button>
        <p>Se a confirmação falhar, consulte a lista abaixo antes de repetir. A publicação não ativa sincronização automática entre canais.</p>
      </section>
      <section className="form-card"><h2>Seus anúncios de teste</h2>
        {itens.length ? <div className="panel-actions">{itens.map(id => <button className="text-button" key={id} onClick={() => {setItemId(id);setItem(null);setFrete(null);}}>{id}</button>)}</div> : <p>A conta conectada ainda não possui anúncios. Publique uma peça de teste no Mercado Livre para consultá-la aqui.</p>}
        <label>Código do anúncio Mercado Livre<input value={itemId} onChange={e => {setItemId(e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, ''));setItem(null);setFrete(null);}} placeholder="MLB1234567890" maxLength={23} /></label>
        <button className="btn full" disabled={ocupado || !/^MLB\d{6,20}$/.test(itemId)} onClick={() => void executar(async () => {
          setItem(await api<Item>(`${base}/itens/${itemId}`, {}, true));setFrete(null);
        })}>Consultar anúncio</button>
        {item && <div role="status"><h3>{item.title}</h3><p>{item.price.toLocaleString('pt-BR', {style:'currency',currency:item.currency_id})} · {item.available_quantity} unidades · {item.status}</p></div>}
      </section>
      {item && <section className="form-card"><h2>Opções de envio</h2>
        <label>CEP de destino<input value={cep} onChange={e => setCep(e.target.value.replace(/\D/g, '').slice(0, 8))} inputMode="numeric" autoComplete="postal-code" maxLength={8} /></label>
        <button className="btn full" disabled={ocupado || cep.length !== 8} onClick={() => void executar(async () => {
          setFrete(await api<Frete>(`${base}/itens/${item.id}/frete?cep=${cep}`, {}, true));
        })}>Consultar frete</button>
        <p>A consulta usa o anúncio selecionado no Mercado Livre. Não contrata envio nem emite etiqueta para pedidos pagos no ISAC.</p>
        {frete && <div role="status">{frete.options?.length ? frete.options.map(opcao => <p key={opcao.id}><strong>{opcao.name}</strong>: {opcao.cost.toLocaleString('pt-BR', {style:'currency',currency:opcao.currency_id || 'BRL'})}{opcao.estimated_delivery_time?.date ? ` · previsão ${new Date(opcao.estimated_delivery_time.date).toLocaleDateString('pt-BR')}` : ''}</p>) : <p>O Mercado Livre não retornou opções para este anúncio e destino.</p>}</div>}
      </section>}
      <section className="form-card"><h2>Associar uma peça do ISAC</h2><p>Oculte a peça no ISAC antes de vincular. A mesma peça não poderá ser reativada aqui enquanto estiver vinculada ao Mercado Livre. Não há sincronização automática entre canais nesta versão.</p>
        <label>Número da peça no ISAC<input inputMode="numeric" value={anuncioId} onChange={e => setAnuncioId(e.target.value.replace(/\D/g, ''))} /></label>
        <p>Anúncio selecionado: {itemId || 'selecione acima'}</p>
        <button className="btn full" disabled={ocupado || !anuncioId || !/^MLB\d{6,20}$/.test(itemId)} onClick={() => void executar(async () => {
          await api(`${base}/vinculos`, {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({anuncioId:Number(anuncioId),itemId})}, true);
          await atualizar();setMensagem('Peça associada. O canal de venda desta peça continua no Mercado Livre.');
        })}>Associar peça oculta</button>
        {vinculos.map(v => <p key={v.anuncioId}>Peça #{v.anuncioId} → {v.itemId} · {v.statusExterno} · estoque consultado: {v.estoqueExterno}</p>)}
      </section>
    </>}
    {erro && <p className="form-error" role="alert">{erro}</p>}
    {mensagem && <p role="status">{mensagem}</p>}
    <p><Link to="/perfil">Voltar ao perfil</Link></p>
  </>;
}
