import type { Theme } from "../lib/useTheme";
import { ExternalLinkIcon, MenuIcon, MoonIcon, SunIcon } from "./icons";

type Props = {
  title: string;
  siteUrl?: string;
  sidebarOpen: boolean;
  onMenuClick: () => void;
  theme: Theme;
  onThemeToggle: () => void;
};

const ICON_BUTTON =
  "flex size-8 flex-none items-center justify-center rounded-lg border border-neutral-300 text-neutral-700 transition-colors hover:bg-neutral-100 dark:border-neutral-700 dark:text-neutral-300 dark:hover:bg-neutral-800";

export function FeedHeader({
  title,
  siteUrl,
  sidebarOpen,
  onMenuClick,
  theme,
  onThemeToggle,
}: Props) {
  return (
    <header className="flex flex-none items-center gap-2.5 border-b border-neutral-200 bg-white px-3 py-2.5 lg:px-5 lg:py-3.5 dark:border-neutral-800 dark:bg-neutral-950">
      <button
        type="button"
        onClick={onMenuClick}
        aria-label={sidebarOpen ? "소스 목록 접기" : "소스 목록 펼치기"}
        aria-expanded={sidebarOpen}
        className={ICON_BUTTON}
      >
        <MenuIcon className="size-5" />
      </button>

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

      <button
        type="button"
        onClick={onThemeToggle}
        aria-label={theme === "dark" ? "밝은 화면으로 전환" : "어두운 화면으로 전환"}
        className={`ml-auto ${ICON_BUTTON}`}
      >
        {theme === "dark" ? <SunIcon className="size-5" /> : <MoonIcon className="size-5" />}
      </button>
    </header>
  );
}
