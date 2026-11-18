import { useEffect } from "react";
import { Capacitor } from "@capacitor/core";
import { App as NativeApp } from "@capacitor/app";
import { useLocation, useNavigate } from "react-router-dom";

/** O botão Voltar do Android acompanha a navegação do aplicativo. */
export function NativeNavigation() {
  const location = useLocation();
  const navigate = useNavigate();
  useEffect(() => {
    if (!Capacitor.isNativePlatform()) return;
    const listener = NativeApp.addListener("backButton", ({ canGoBack }) => {
      if (["/", "/catalogo", "/boas-vindas"].includes(location.pathname) && !canGoBack) void NativeApp.exitApp();
      else if (canGoBack) navigate(-1);
      else navigate("/", { replace: true });
    });
    return () => { void listener.then(handle => handle.remove()); };
  }, [location.pathname, navigate]);
  return null;
}
