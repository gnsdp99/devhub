import { useParams } from "react-router";
import { ArticleFeed } from "../components/ArticleFeed";
import { FeedHeader } from "../components/FeedHeader";
import { useDocumentTitle } from "../lib/useDocumentTitle";
import { useAppLayout } from "./AppLayout";

export function SourcePage() {
  const { slug = "" } = useParams();
  const { sources } = useAppLayout();

  const source = sources?.find((candidate) => candidate.slug === slug);
  const isUnknown = sources !== undefined && source === undefined;
  const title = isUnknown ? "알 수 없는 소스" : (source?.name ?? "");
  useDocumentTitle(title);

  return (
    <>
      <FeedHeader title={title} siteUrl={source?.siteUrl} logoUrl={source?.logoUrl} />
      {isUnknown ? (
        <div className="flex flex-1 items-center justify-center px-6 text-center">
          <p className="text-body text-ink-muted">
            "{slug}" 소스를 찾을 수 없어요. 왼쪽 목록에서 다시 골라 주세요.
          </p>
        </div>
      ) : (
        // key로 소스가 바뀔 때마다 목록을 다시 마운트해 스크롤을 맨 위에서 시작시킨다.
        <ArticleFeed key={slug} source={slug} showSourceName={false} />
      )}
    </>
  );
}
