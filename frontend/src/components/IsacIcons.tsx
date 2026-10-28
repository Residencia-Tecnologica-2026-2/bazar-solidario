import { useId, type SVGProps } from "react";
type IconProps = SVGProps<SVGSVGElement> & { size?: number; strokeWidth?: number };
/** Original Figma assets. Masks only apply the surrounding UI color; geometry is unchanged. */
function assetIcon(file: string, preserveColor = false) {
  return function IsacIcon({ size = 24, strokeWidth: _strokeWidth, className = "", ...props }: IconProps) {
    const id = useId().replace(/:/g, "");
    const source = "/icons/isac/" + file;
    return <svg width={size} height={size} viewBox="0 0 24 24" className={`isac-icon ${className}`} aria-hidden="true" focusable="false" {...props}>
      {preserveColor ? <image href={source} width="24" height="24" preserveAspectRatio="xMidYMid meet" /> : <>
        <defs><mask id={id} maskUnits="userSpaceOnUse" x="0" y="0" width="24" height="24" style={{ maskType: "alpha" }}><image href={source} width="24" height="24" preserveAspectRatio="xMidYMid meet" /></mask></defs>
        <rect width="24" height="24" fill="currentColor" mask={`url(#${id})`} />
      </>}
    </svg>;
  };
}
export const Home = assetIcon("Home.png", false);
export const Search = assetIcon("Search.png", false);
/** Original tag-and-star artwork exported from the two Figma variants. */
export function FavoriteIcon({size=24,strokeWidth:_strokeWidth,className="",...props}:IconProps) {
  return <svg width={size} height={size} viewBox="0 0 24 24" className={`isac-icon favorite-state-icon ${className}`} aria-hidden="true" focusable="false" {...props}>
    <image className="favorite-icon-off" href="/icons/isac/favoritos-nao-salvo.svg" x="4.794" y="2" width="14.412" height="20" />
    <image className="favorite-icon-on" href="/icons/isac/favoritos-salvo.svg" x="4.794" y="2" width="14.412" height="20" />
  </svg>;
}
// Existing call sites retain their semantic API; no replacement artwork is drawn in code.
export const Heart = FavoriteIcon;
export const CalendarDays = assetIcon("CalendarDays.png", false);
export const UserRound = assetIcon("UserRound.png", false);
export const Gem = assetIcon("Gem.svg", true);
export const Footprints = assetIcon("Footprints.svg", true);
export const Leaf = assetIcon("Leaf.svg", true);
export const HandHeart = assetIcon("HandHeart.svg", true);
export const Store = assetIcon("Store.svg", true);
export const ShoppingBag = assetIcon("ShoppingBag.png", false);
export const SlidersHorizontal = assetIcon("SlidersHorizontal.png", false);
export const Plus = assetIcon("Plus.png", true);
export const Trash2 = assetIcon("Trash2.png", false);
export const ArrowRight = assetIcon("ArrowRight.svg", false);
export const ArrowUpRight = assetIcon("ArrowUpRight.svg", false);
export const ChevronRight = assetIcon("ChevronRight.svg", false);
export const ChevronDown = assetIcon("ChevronDown.svg", false);
export const MapPin = assetIcon("MapPin.png", false);
export const Info = assetIcon("Info.png", false);
export const CheckCircle2 = assetIcon("CheckCircle2.svg", false);
export const Check = assetIcon("Check.svg", false);
export function Shirt({size=24,strokeWidth:_strokeWidth,className="",...props}:IconProps) {
  return <svg width={size} height={size} viewBox="0 0 88.061 65.881" className={`isac-icon ${className}`} aria-hidden="true" focusable="false" {...props}>
    <image href="/icons/isac/Shirt1.svg" width="88.061" height="65.881" />
    <image href="/icons/isac/Shirt.svg" x="32.15" y="0.54" width="23.758" height="11.069" />
  </svg>;
}
// Shared 24px Figma components include optical spacing; never fit raw artwork here.
export const ClothingIcon=assetIcon("categoria-roupas.svg", true);
export const AccessoriesIcon=assetIcon("categoria-acessorios.svg", true);
export const ShoesIcon=assetIcon("categoria-calcados.svg", true);
export function SavedIcon(props:IconProps){return <FavoriteIcon {...props} className={`favorite-category-icon ${props.className||""}`}/>;}
export function ArrowLeft(props:IconProps){return <ArrowRight {...props} style={{...props.style,transform:"rotate(180deg)"}}/>;}

