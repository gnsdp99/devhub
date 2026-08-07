import { NavLink } from "react-router";
import type { Source } from "../api/sources";
import { FOCUS_RING, ICON_BUTTON } from "../lib/styles";
import { ChevronLeftIcon } from "./icons";
import { SourceMark } from "./SourceMark";

type Props = {
  sources: Source[] | undefined;
  isPending: boolean;
  errorMessage: string | undefined;
  query: string;
  onQueryChange: (query: string) => void;
  drawerOpen: boolean;
  desktopOpen: boolean;
  onClose: () => void;
  onSelect: () => void;
};

/** 소스 이름 길이는 제각각이라, 뼈대도 폭을 흩뜨려야 목록처럼 읽힌다. */
const SKELETON_WIDTHS = ["72%", "88%", "61%", "80%", "67%", "84%"];

/** 항목과 뼈대가 같은 높이여야 목록이 채워질 때 줄이 튀지 않는다. */
const ROW = "min-h-10 flex-none px-3 py-2";

export function Sidebar({
  sources,
  isPending,
  errorMessage,
  query,
  onQueryChange,
  drawerOpen,
  desktopOpen,
  onClose,
  onSelect,
}: Props) {
  const keyword = query.trim().toLowerCase();
  const entries = sources ?? [];
  const visible = keyword
    ? entries.filter((source) => source.name.toLowerCase().includes(keyword))
    : entries;

  return (
    <aside
      className={[
        "absolute inset-y-0 left-0 z-40 flex w-[270px] flex-col border-r border-line bg-panel transition-transform duration-200 ease-out",
        "lg:static lg:z-auto lg:w-[238px] lg:translate-x-0 lg:shadow-none lg:transition-none",
        drawerOpen ? "translate-x-0 shadow-2xl" : "-translate-x-full",
        desktopOpen ? "lg:flex" : "lg:hidden",
      ].join(" ")}
    >
      <div className="flex h-14 flex-none items-center gap-2 border-b border-line px-3">
        <input
          type="search"
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
          placeholder="소스 이름 검색…"
          aria-label="소스 이름 검색"
          className={`min-w-0 flex-1 rounded-lg border border-line bg-surface px-3 py-1.5 text-body text-ink transition-colors placeholder:text-ink-faint hover:border-line-strong ${FOCUS_RING}`}
        />
        <button type="button" onClick={onClose} aria-label="소스 목록 닫기" className={ICON_BUTTON}>
          <ChevronLeftIcon className="size-4" />
        </button>
      </div>

      <nav className="flex flex-1 flex-col gap-1 overflow-y-auto p-2">
        {isPending &&
          SKELETON_WIDTHS.map((width) => (
            <div key={width} className={`flex items-center ${ROW}`}>
              <div
                className="h-4 animate-pulse rounded-sm bg-line"
                style={{ width }}
                aria-hidden="true"
              />
            </div>
          ))}

        {errorMessage && <p className="px-3 py-2 text-body text-ink-muted">{errorMessage}</p>}

        {!isPending && !errorMessage && visible.length === 0 && (
          <p className="px-3 py-2 text-body text-ink-faint">일치하는 소스가 없어요</p>
        )}

        {visible.map((source) => (
          <NavLink
            key={source.slug}
            to={`/sources/${source.slug}`}
            onClick={onSelect}
            className={({ isActive }) =>
              [
                `group flex items-center gap-2 rounded-lg text-body transition-colors ${ROW} ${FOCUS_RING}`,
                isActive
                  ? "bg-tint font-semibold text-ink"
                  : "text-ink-muted hover:bg-tint/50 hover:text-ink",
              ].join(" ")
            }
          >
            {({ isActive }) => (
              <>
                {/* 로고 색은 소스를 알아보는 단서라 지우지 않는다. 고르지 않은 항목만 한 겹 물린다. */}
                <SourceMark
                  src={source.logoUrl}
                  className={`size-[18px] transition-opacity ${
                    isActive ? "" : "opacity-75 group-hover:opacity-100"
                  }`}
                />
                <span className="truncate">{source.name}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
