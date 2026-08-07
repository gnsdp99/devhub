import { Link } from "react-router";
import { FeedHeader } from "../components/FeedHeader";
import { OUTLINE_BUTTON } from "../lib/styles";
import { useDocumentTitle } from "../lib/useDocumentTitle";

export function NotFoundPage() {
  useDocumentTitle("찾을 수 없는 페이지");

  return (
    <>
      <FeedHeader title="찾을 수 없는 페이지" />
      <div className="flex flex-1 flex-col items-center justify-center gap-3.5 px-6 text-center">
        <p className="text-heading text-ink-muted">주소를 찾을 수 없어요</p>
        <Link to="/" className={OUTLINE_BUTTON}>
          전체 피드로 가기
        </Link>
      </div>
    </>
  );
}
