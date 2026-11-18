import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, ArrowRight, Check, Heart, Leaf, ShoppingBag, Shirt, Store, CheckCircle2, HandHeart } from "./IsacIcons";
import { Brand } from "./Brand";
import "../styles/apresentacao.css";

export const CHAVE_APRESENTACAO = "isac-apresentacao-v1";
const etapas = [
  { titulo: "Roupa boa. História nova.", texto: "Aqui você encontra peças de brechós e ajuda a transformar a comunidade. Uma nova vida para a roupa. Um novo começo para alguém.", acao: "Vamos descobrir" },
  { titulo: "Gostou? Guarde seu achado.", texto: "Explore as peças e toque no marcador para salvar suas favoritas. Experimente na peça abaixo.", acao: "Entendi, próximo" },
  { titulo: "Sua escolha tem propósito.", texto: "Coloque a peça na sacola. 10% do valor dos produtos é destinado à ISAC; o restante vai para o brechó, descontadas as tarifas do pagamento.", acao: "Como acompanho?" },
  { titulo: "Tudo junto, em Meus pedidos.", texto: "Registre o pedido e acompanhe as atualizações. O aceite do brechó e o pagamento são etapas separadas. Entrega ou retirada é combinada com a loja.", acao: "Começar a explorar" },
];

export function Apresentacao() {
  const [etapa, setEtapa] = useState(0);
  const [salvo, setSalvo] = useState(false);
  const [sacola, setSacola] = useState(false);
  const [acompanhando, setAcompanhando] = useState(false);
  const titulo = useRef<HTMLHeadingElement>(null);
  const navigate = useNavigate();
  const atual = etapas[etapa];
  useEffect(() => { titulo.current?.focus(); }, [etapa]);
  function concluir() {
    try { localStorage.setItem(CHAVE_APRESENTACAO, "concluida"); } catch { /* O app também funciona sem armazenamento. */ }
    navigate("/catalogo", { replace: true });
  }
  return <section className="intro-isac" aria-label="Conheça o ISAC Brechó">
    <header className="intro-top"><Brand /><button className="text-button" onClick={concluir}>Pular apresentação <ArrowRight size={16} /></button></header>
    <div className="intro-progress" aria-label={`Passo ${etapa + 1} de ${etapas.length}`}>
      {etapas.map((item, i) => <button key={item.titulo} aria-label={`Ir para o passo ${i + 1}`} aria-current={i === etapa ? "step" : undefined} className={i <= etapa ? "preenchido" : ""} onClick={() => setEtapa(i)} />)}
    </div>
    <p className="intro-count">SEU PRIMEIRO ACHADO COMEÇA AQUI · {etapa + 1}/4</p>
    <div className="intro-art" key={etapa}>
      {etapa === 0 ? <div className="intro-purpose"><span className="intro-orbit"><Leaf size={28} /></span><div className="intro-shirt"><Shirt size={105} strokeWidth={1.2} /><span>ISAC</span></div><span className="intro-seal"><HandHeart size={23} /> Moda que faz bem</span><div className="intro-mini-label"><Store size={18} /> Brechós + você + comunidade</div></div>
      : etapa === 1 || etapa === 2 ? <div className="intro-demo-product">
        <div className="intro-product-art"><Shirt size={100} strokeWidth={1.2} /><span className="intro-sample">PEÇA DE EXEMPLO</span>
          {etapa === 1 && <button className={`intro-heart ${salvo ? "selected" : ""}`} aria-label={salvo ? "Remover exemplo dos favoritos" : "Salvar exemplo nos favoritos"} aria-pressed={salvo} onClick={() => setSalvo(!salvo)}><Heart size={24}  /></button>}
        </div>
        <div className="intro-product-info"><span>BRECHÓ DA COMUNIDADE</span><h2>Uma peça, novas histórias.</h2><div className="intro-product-price"><strong>R$ 40,00</strong>{etapa === 2 && <button className="intro-add" aria-label={sacola ? "Retirar exemplo da sacola" : "Adicionar exemplo à sacola"} aria-pressed={sacola} onClick={() => setSacola(!sacola)}>{sacola ? <Check size={23} /> : <ShoppingBag size={23} />}</button>}</div></div>
        <p className="intro-feedback" role="status">{etapa === 1 ? salvo ? "Salvou! Seus favoritos ficam na aba Salvos." : "Toque no marcador. É só um ensaio :)" : sacola ? "Na sacola! R$ 4,00 deste exemplo apoiariam a ISAC." : "Toque na sacola para experimentar."}</p>
      </div> : <div className="intro-order"><div className="intro-order-head"><CheckCircle2 size={31} /><div><span>EXEMPLO DE ACOMPANHAMENTO</span><strong>Seu achado, passo a passo.</strong></div></div><ol><li><Check size={17} /><div><strong>Pedido registrado</strong><span>A peça fica reservada para você.</span></div></li><li className={acompanhando ? "concluido" : ""}><Store size={17} /><div><strong>{acompanhando ? "Brechó confirmou" : "Aguardando o brechó"}</strong><span>Combine a entrega ou retirada com a loja.</span></div></li><li><HandHeart size={17} /><div><strong>Pagamento acompanhado à parte</strong><span>Nesta versão, Pix apenas em ambiente de teste.</span></div></li></ol><button className="intro-preview" onClick={() => setAcompanhando(!acompanhando)}>{acompanhando ? "Rever exemplo" : "Ver uma atualização"}<ArrowRight size={17} /></button><p className="intro-feedback" role="status">{acompanhando ? "É aqui que você confere cada atualização." : "Experimente acompanhar o pedido."}</p></div>}
    </div>
    <div className="intro-copy"><h1 ref={titulo} tabIndex={-1}>{atual.titulo}</h1><p>{atual.texto}</p></div>
    <div className="intro-actions"><button className="btn full" onClick={() => etapa === 3 ? concluir() : setEtapa(etapa + 1)}>{atual.acao}<ArrowRight size={19} /></button>{etapa > 0 && <button className="text-button" onClick={() => setEtapa(etapa - 1)}><ArrowLeft size={16} /> Voltar um passo</button>}</div>
    <p className="intro-footnote">Um ensaio, sem compras ou reservas. Reveja quando quiser no Perfil.</p>
  </section>;
}
