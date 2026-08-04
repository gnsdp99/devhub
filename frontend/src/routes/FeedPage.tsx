import { ArticleFeed } from "../components/ArticleFeed";
import { FeedHeader } from "../components/FeedHeader";
import { useDocumentTitle } from "../lib/useDocumentTitle";

export function FeedPage() {
  // 전체 피드는 사이트의 첫 화면이라 탭 제목에 덧붙일 대상이 없다.
  useDocumentTitle();

  return (
    <>
      <FeedHeader title="전체 피드" />
      <ArticleFeed showSourceName />
    </>
  );
}
