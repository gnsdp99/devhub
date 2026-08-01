import { Link } from "react-router";
import { FeedHeader } from "../components/FeedHeader";
import { useDocumentTitle } from "../lib/useDocumentTitle";
import { useAppLayout } from "./AppLayout";

export function NotFoundPage() {
  const { sidebarOpen, toggleSidebar } = useAppLayout();
  useDocumentTitle("찾을 수 없는 페이지");

  return (
    <>
      <FeedHeader
        title="찾을 수 없는 페이지"
        sidebarOpen={sidebarOpen}
        onMenuClick={toggleSidebar}
      />
      <div className="flex flex-1 flex-col items-center justify-center gap-3.5 px-6 text-center">
        <p className="text-lg text-neutral-600">주소를 찾을 수 없어요</p>
        <Link
          to="/"
          className="rounded-full border border-neutral-900 px-4 py-1.5 text-sm text-neutral-900 transition-colors hover:bg-neutral-900 hover:text-white"
        >
          전체 피드로 가기
        </Link>
      </div>
    </>
  );
}
