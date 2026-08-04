import { Link } from "react-router";
import type { Theme } from "../lib/useTheme";
import { MenuIcon, MoonIcon, SunIcon } from "./icons";

type Props = {
  sidebarOpen: boolean;
  onMenuClick: () => void;
  theme: Theme;
  onThemeToggle: () => void;
};

const ICON_BUTTON =
  "flex size-8 flex-none items-center justify-center rounded-lg border border-neutral-300 text-neutral-700 transition-colors hover:bg-neutral-100 dark:border-neutral-700 dark:text-neutral-300 dark:hover:bg-neutral-800";

/** 사이드바와 본문 위를 가로지르는 전역 바. 어느 화면에서나 같은 자리에 남는다. */
export function TopBar({ sidebarOpen, onMenuClick, theme, onThemeToggle }: Props) {
  return (
    <header className="flex h-14 flex-none items-center gap-2.5 border-b border-neutral-200 bg-white px-3 lg:px-5 dark:border-neutral-800 dark:bg-neutral-950">
      <button
        type="button"
        onClick={onMenuClick}
        aria-label={sidebarOpen ? "소스 목록 접기" : "소스 목록 펼치기"}
        aria-expanded={sidebarOpen}
        className={ICON_BUTTON}
      >
        <MenuIcon className="size-5" />
      </button>

      <Link
        to="/"
        className="flex-none rounded text-lg font-bold tracking-tight text-neutral-900 transition-colors hover:text-neutral-600 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-neutral-900 lg:text-xl dark:text-neutral-100 dark:hover:text-neutral-400 dark:focus-visible:outline-neutral-100"
      >
        DevHub
      </Link>

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
