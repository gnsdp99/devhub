import { READING_COLUMN } from "../lib/layout";
import { FOCUS_RING } from "../lib/styles";
import { ExternalLinkIcon } from "./icons";
import { SourceMark } from "./SourceMark";

type Props = {
  title: string;
  siteUrl?: string;
  logoUrl?: string | null;
};

/** 본문 칸에만 걸리는 제목 줄. 지금 보고 있는 피드가 무엇인지 알린다. */
export function FeedHeader({ title, siteUrl, logoUrl }: Props) {
  return (
    // h-14는 사이드바 검색줄과 같은 높이다. 두 칸의 경계선이 한 줄로 이어진다.
    <div className="h-14 flex-none border-b border-line">
      {/* 경계선은 폭 전체를 긋되, 제목은 카드와 같은 열 안에 두어 왼쪽 끝이 맞는다. */}
      <div className={`${READING_COLUMN} flex h-full items-center gap-2.5 px-3 lg:px-4`}>
        <SourceMark src={logoUrl} className="size-6" />

        {title ? (
          <h1 className="truncate text-heading font-semibold text-ink">{title}</h1>
        ) : (
          <div className="h-6 w-40 animate-pulse rounded-sm bg-line" aria-hidden="true" />
        )}

        {siteUrl && (
          <a
            href={siteUrl}
            target="_blank"
            rel="noreferrer"
            className={`flex flex-none items-center gap-1.5 rounded-sm text-ink-faint transition-colors hover:text-ink ${FOCUS_RING}`}
          >
            <span className="flex size-6 items-center justify-center">
              <ExternalLinkIcon className="size-4" />
            </span>
            <span className="hidden text-meta lg:inline">원본 사이트</span>
          </a>
        )}
      </div>
    </div>
  );
}
