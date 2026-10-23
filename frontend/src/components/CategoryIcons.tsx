import type { ReactNode, SVGProps } from "react";
type Props = SVGProps<SVGSVGElement> & { size?: number; strokeWidth?: number };
const frame = (content: ReactNode) => function CategoryIcon({size=32,strokeWidth=1.8,...props}:Props){return <svg width={size} height={size} viewBox="0 0 32 32" fill="none" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" focusable="false" {...props}>{content}</svg>};
export const ClothingIcon=frame(<path d="M11 6 3 11l4 6 4-2v12h10V15l4 2 4-6-8-5c0 5-10 5-10 0Z"/>);
export const AccessoriesIcon=frame(<path d="m9 7-6 7 13 13 13-13-6-7H9Zm-6 7h26M9 7l7 20L23 7"/>);
export const ShoesIcon=frame(<path d="M5 5h8l2 10 11 5c2 1 2 3 2 6H4L5 5Zm-1 17h24M15 15l-3 3m8-1-3 3"/>);
export const SavedIcon=frame(<path d="M16 27S4 19 4 11a7 7 0 0 1 12-4 7 7 0 0 1 12 4c0 8-12 16-12 16Z"/>);
