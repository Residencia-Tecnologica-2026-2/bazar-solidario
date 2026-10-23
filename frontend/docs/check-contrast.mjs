import fs from 'node:fs';
const luminance=hex=>{const rgb=hex.replace('#','').match(/../g).map(v=>parseInt(v,16)/255).map(v=>v<=.04045?v/12.92:((v+.055)/1.055)**2.4);return rgb.reduce((n,v,i)=>n+v*[.2126,.7152,.0722][i],0)};
const pairs=[['texto / fundo','#40291E','#F1EBCB'],['texto / laranja','#40291E','#F57F3A'],['texto / pêssego','#40291E','#FF9B5A'],['ação / texto','#F1EBCB','#0B6676'],['texto secundário / fundo','#654535','#F1EBCB']];
const results=pairs.map(([name,foreground,background])=>{const a=luminance(foreground),b=luminance(background),ratio=(Math.max(a,b)+.05)/(Math.min(a,b)+.05);return {name,foreground,background,ratio:Number(ratio.toFixed(2)),AA:ratio>=4.5}});
fs.writeFileSync(new URL('contraste.json',import.meta.url),JSON.stringify(results,null,2));
console.log(results);
if(results.some(p=>!p.AA))process.exitCode=1;
