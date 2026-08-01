import { ExternalLinkIcon, MenuIcon } from "./icons";

type Props = {
  title: string;
  siteUrl?: string;
  sidebarOpen: boolean;
  onMenuClick: () => void;
};

export function FeedHeader({ title, siteUrl, sidebarOpen, onMenuClick }: Props) {
  return (
    <header className="flex flex-none items-center gap-2.5 border-b border-neutral-200 bg-white px-3 py-2.5 lg:px-5 lg:py-3.5">
      <button
        type="button"
        onClick={onMenuClick}
        aria-label={sidebarOpen ? "소스 목록 접기" : "소스 목록 펼치기"}
        aria-expanded={sidebarOpen}
        className="flex size-8 flex-none items-center justify-center rounded-lg border border-neutral-300 text-neutral-700 transition-colors hover:bg-neutral-100"
      >
        <MenuIcon className="size-5" />
      </button>

      {title ? (
        <h1 className="truncate text-lg font-semibold text-neutral-900 lg:text-xl">{title}</h1>
      ) : (
        <div className="h-6 w-40 animate-pulse rounded bg-neutral-200" />
      )}

      {siteUrl && (
        <a
          href={siteUrl}
          target="_blank"
          rel="noreferrer"
          className="flex flex-none items-center gap-1.5 text-neutral-500 transition-colors hover:text-neutral-900"
        >
          <span className="flex size-6 items-center justify-center rounded-md border border-neutral-300">
            <ExternalLinkIcon className="size-3.5" />
          </span>
          <span className="hidden text-xs lg:inline">원본 사이트</span>
        </a>
      )}
    </header>
  );
}
