import type { SVGProps } from 'react';
type Props = SVGProps<SVGSVGElement> & { size?: number; strokeWidth?: number };
const icon = (paths: string[]) => function CoastalIcon({size=24,strokeWidth=1.8,...props}:Props){return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...props}>{paths.map((d,i)=><path key={i} d={d}/>)}</svg>};
// Curvas abertas e assimetrias leves: a mesma família, funções reconhecíveis.
export const Home=icon(['M3 11 11.5 3 21 10','M5 9.5v10h5v-6h5v6h5v-10','M3 22q5-2 9 0t9 0']);
export const Search=icon(['M17 10a7 7 0 1 1-14 0 7 7 0 0 1 14 0','m15 15 6 6','M6 8q1-3 4-3']);
export const Heart=icon(['M12 21C8 17 2 13 3 7c1-5 7-5 9-1 3-4 8-3 9 1 1 5-5 11-9 14','M6 8q0-2 2-2']);
export const ShoppingBag=icon(['M5 8 3 21h18L19 8Z','M8 9V6a4 4 0 0 1 8 0v3','M8 15q4 3 8-1']);
export const UserRound=icon(['M15.8 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0','M3.5 21q1-9 8.5-9t8.5 9','M8 19q4 2 8 0']);
export const CalendarDays=icon(['M4 5h16v16H4Z','M8 2v5m8-5v5M4 10h16','M8 14h1m5 0h2M8 17h1m5 0h2']);
export const Shirt=icon(['m8 3-6 4 3 5 3-2-1 11h10l-1-11 3 2 3-5-6-4','M8 3q4 6 8 0','M10 17q2 1 4 0']);
export const Gem=icon(['M5 4h14l3 6-10 11L2 10Z','M2 10h20M8 4l-2 6 6 11 6-11-2-6']);
export const Footprints=icon(['M7 2q-4 0-4 5v7h6V7q0-5-2-5','M3 18v3h6v-3Z','M16 5q-3 0-3 5v7h6v-7q0-5-3-5','M13 21h6']);
export const Leaf=icon(['M3 20Q1 5 21 3q0 17-14 15','M3 21 17 7','M9 14V9m4 1h5']);
export const HandHeart=icon(['M2 16 6 13h5q4 0 4 3H8','M2 21h6l12-6q3-2 1-3l-6 2','M14 10q-7-5-4-8 2-2 4 1 3-3 5-1 3 3-5 8']);
