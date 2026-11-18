import "@fontsource-variable/fraunces";
import "@fontsource-variable/instrument-sans";
import "./styles/tokens.css";
import "./styles/global.css";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { NativeNavigation } from "./components/NativeNavigation";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <NativeNavigation />
      <App />
    </BrowserRouter>
  </StrictMode>,
);

import './styles/icons.css';
import './styles/forms.css';
import './styles/product-detail.css';
