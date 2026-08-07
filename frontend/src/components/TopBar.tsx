import { Link } from "react-router";
import { FOCUS_RING, ICON_BUTTON } from "../lib/styles";
import type { Theme } from "../lib/useTheme";
import { MenuIcon, MoonIcon, SunIcon } from "./icons";

type Props = {
  sidebarOpen: boolean;
  onMenuClick: () => void;
  theme: Theme;
  onThemeToggle: () => void;
};

/** 사이드바와 본문 위를 가로지르는 전역 바. 어느 화면에서나 같은 자리에 남는다. */
export function TopBar({ sidebarOpen, onMenuClick, theme, onThemeToggle }: Props) {
  return (
    <header className="flex h-14 flex-none items-center gap-2.5 border-b border-line bg-canvas px-3 lg:px-5">
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
        className={`flex-none rounded-sm text-wordmark font-bold text-ink transition-colors hover:text-ink-muted ${FOCUS_RING}`}
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
