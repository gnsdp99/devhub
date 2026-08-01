import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router";
import { AppLayout } from "./routes/AppLayout";
import { ErrorPage } from "./routes/ErrorPage";
import { FeedPage } from "./routes/FeedPage";
import { NotFoundPage } from "./routes/NotFoundPage";
import { SourcePage } from "./routes/SourcePage";
import "./index.css";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
});

const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    // 레이아웃까지 무너진 경우라 사이드바 없이 단독으로 보여준다.
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <FeedPage /> },
      { path: "sources/:slug", element: <SourcePage /> },
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);

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
