import { ArticleFeed } from "../components/ArticleFeed";
import { FeedHeader } from "../components/FeedHeader";
import { useDocumentTitle } from "../lib/useDocumentTitle";
import { useAppLayout } from "./AppLayout";

export function FeedPage() {
  const { sidebarOpen, toggleSidebar } = useAppLayout();
  useDocumentTitle("전체 피드");

  return (
    <>
      <FeedHeader title="전체 피드" sidebarOpen={sidebarOpen} onMenuClick={toggleSidebar} />
      <ArticleFeed showSourceName />
    </>
  );
}
