import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router";
import { FeedPage } from "./routes/FeedPage";
import "./index.css";

const queryClient = new QueryClient();

const router = createBrowserRouter([{ path: "/", element: <FeedPage /> }]);

const rootElement = document.getElementById("root");
if (!rootElement) {
  throw new Error("#root 엘리먼트를 찾을 수 없습니다");
}

createRoot(rootElement).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
);
