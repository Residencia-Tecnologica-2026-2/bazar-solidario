export const products = [
  {
    id: 1,
    name: "Camisa de linho",
    category: "Roupas",
    size: "M",
    price: 65,
    shop: "Brechó Raízes",
    color: "Areia",
    image: "photo-1598554747436-c9293d6a588f",
    description:
      "Leve, versátil e fácil de combinar. Uma peça para acompanhar novos dias e novas histórias.",
  },
  {
    id: 2,
    name: "Bolsa caramelo",
    category: "Acessórios",
    size: "Único",
    price: 89,
    shop: "Circular Brechó",
    color: "Caramelo",
    image: "/products/bolsa-caramelo-sem-marca.png",
    description:
      "Uma bolsa de linhas clássicas para levar o essencial com personalidade.",
  },
  {
    id: 3,
    name: "Jaqueta jeans",
    category: "Roupas",
    size: "G",
    price: 95,
    shop: "Brechó Raízes",
    color: "Azul",
    image: "photo-1543076447-215ad9ba6923",
    description:
      "Um clássico que atravessa temporadas. Combine com suas peças favoritas e crie novas possibilidades.",
  },
  {
    id: 4,
    name: "Tênis casual",
    category: "Calçados",
    size: "38",
    price: 75,
    shop: "Novo Ciclo",
    color: "Caramelo",
    image: "/products/tenis-casual-sem-marca.png",
    description:
      "Estilo casual para os seus próximos caminhos. Confira a numeração e os detalhes antes de escolher.",
  },
  {
    id: 5,
    name: "Vestido leve",
    category: "Roupas",
    size: "P",
    price: 58,
    shop: "Circular Brechó",
    color: "Natural",
    image: "photo-1515372039744-b8f02a3ae446",
    description:
      "Uma peça delicada para renovar as combinações do seu guarda-roupa.",
  },
  {
    id: 6,
    name: "Óculos clássico",
    category: "Acessórios",
    size: "Único",
    price: 35,
    shop: "Novo Ciclo",
    color: "Preto",
    image: "photo-1511499767150-a48a237f0083",
    description:
      "Linhas clássicas e personalidade nos detalhes. Especificações de proteção devem ser verificadas pelo parceiro.",
  },
];
export type Product = (typeof products)[number] & { vendedorId?:number };
export const photo = (id: string, width = 700) =>
  (id.startsWith("/") || /^https?:\/\//.test(id))
    ? id
    : `https://images.unsplash.com/${id}?auto=format&fit=crop&w=${width}&q=85`;
export const events = [
  {
    id: "feira",
    name: "Achados & afetos",
    kind: "Feira de brechó",
    photo: "photo-1685883785843-049df7767ce1",
    description:
      "Uma feira que reúne brechós independentes na ONG. Descubra peças, conheça quem está por trás de cada achado e se aproxime da comunidade.",
  },
  {
    id: "circular",
    name: "Um novo ciclo",
    kind: "Moda e comunidade",
    photo: "photo-1556905055-8f358a7a47b2",
    description:
      "Uma proposta de encontro para conversar sobre doações, reaproveitamento de roupas e os caminhos da moda circular.",
  },
];
