import { ExternalLinkIcon } from "./icons";

type Props = {
  title: string;
  siteUrl?: string;
};

/** 본문 칸에만 걸리는 제목 줄. 지금 보고 있는 피드가 무엇인지 알린다. */
export function FeedHeader({ title, siteUrl }: Props) {
  return (
    // h-14는 사이드바 검색줄과 같은 높이다. 두 칸의 경계선이 한 줄로 이어진다.
    <div className="flex h-14 flex-none items-center gap-2.5 border-b border-neutral-200 px-3 lg:px-5 dark:border-neutral-800">
      {title ? (
        <h1 className="truncate text-lg font-semibold text-neutral-900 lg:text-xl dark:text-neutral-100">
          {title}
        </h1>
      ) : (
        <div className="h-6 w-40 animate-pulse rounded bg-neutral-200 dark:bg-neutral-800" />
      )}

      {siteUrl && (
        <a
          href={siteUrl}
          target="_blank"
          rel="noreferrer"
          className="flex flex-none items-center gap-1.5 text-neutral-500 transition-colors hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-100"
        >
          <span className="flex size-6 items-center justify-center rounded-md border border-neutral-300 dark:border-neutral-700">
            <ExternalLinkIcon className="size-3.5" />
          </span>
          <span className="hidden text-xs lg:inline">원본 사이트</span>
        </a>
      )}
    </div>
  );
}
