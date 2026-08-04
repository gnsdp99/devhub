import { ArticleFeed } from "../components/ArticleFeed";
import { FeedHeader } from "../components/FeedHeader";
import { useDocumentTitle } from "../lib/useDocumentTitle";
import { useAppLayout } from "./AppLayout";

export function FeedPage() {
  const { sidebarOpen, toggleSidebar, theme, toggleTheme } = useAppLayout();
  // 전체 피드는 사이트의 첫 화면이라 탭 제목에 덧붙일 대상이 없다.
  useDocumentTitle();

  return (
    <>
      <FeedHeader
        title="전체 피드"
        sidebarOpen={sidebarOpen}
        onMenuClick={toggleSidebar}
        theme={theme}
        onThemeToggle={toggleTheme}
      />
      <ArticleFeed showSourceName />
    </>
  );
}
