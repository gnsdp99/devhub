import { NavLink } from "react-router";
import type { Source } from "../api/sources";
import { ChevronLeftIcon } from "./icons";

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

const SKELETON_KEYS = ["a", "b", "c", "d", "e", "f"];

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
        "absolute inset-y-0 left-0 z-40 flex w-[270px] flex-col border-r border-neutral-200 bg-neutral-50 transition-transform duration-200 ease-out",
        "dark:border-neutral-800 dark:bg-neutral-900",
        "lg:static lg:z-auto lg:w-[238px] lg:translate-x-0 lg:shadow-none lg:transition-none",
        drawerOpen ? "translate-x-0 shadow-2xl" : "-translate-x-full",
        desktopOpen ? "lg:flex" : "lg:hidden",
      ].join(" ")}
    >
      <div className="flex h-14 flex-none items-center gap-2 border-b border-neutral-200 px-3 dark:border-neutral-800">
        <input
          type="search"
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
          placeholder="소스 이름 검색…"
          aria-label="소스 이름 검색"
          className="min-w-0 flex-1 rounded-full border border-neutral-300 bg-white px-3.5 py-1.5 text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:outline-none dark:border-neutral-700 dark:bg-neutral-950 dark:text-neutral-100 dark:placeholder:text-neutral-500 dark:focus:border-neutral-100"
        />
        <button
          type="button"
          onClick={onClose}
          aria-label="소스 목록 닫기"
          className="flex size-7 flex-none items-center justify-center rounded-md text-neutral-600 transition-colors hover:bg-neutral-200 dark:text-neutral-300 dark:hover:bg-neutral-800"
        >
          <ChevronLeftIcon className="size-4" />
        </button>
      </div>

      <nav className="flex flex-1 flex-col gap-1 overflow-y-auto p-2">
        {isPending &&
          SKELETON_KEYS.map((key) => (
            <div key={key} className="flex-none px-3 py-2.5">
              <div className="h-4 animate-pulse rounded bg-neutral-200 dark:bg-neutral-800" />
            </div>
          ))}

        {errorMessage && (
          <p className="px-3 py-2 text-sm text-neutral-500 dark:text-neutral-400">{errorMessage}</p>
        )}

        {!isPending && !errorMessage && visible.length === 0 && (
          <p className="px-3 py-2 text-sm text-neutral-400 dark:text-neutral-500">
            일치하는 소스가 없어요
          </p>
        )}

        {visible.map((source) => (
          <NavLink
            key={source.slug}
            to={`/sources/${source.slug}`}
            onClick={onSelect}
            className={({ isActive }) =>
              [
                "flex flex-none items-center gap-2 rounded-r border-l-[3px] px-3 py-2 text-sm transition-colors",
                isActive
                  ? "border-neutral-900 bg-neutral-200 font-semibold text-neutral-900 dark:border-neutral-100 dark:bg-neutral-800 dark:text-neutral-100"
                  : "border-transparent text-neutral-600 hover:bg-neutral-100 dark:text-neutral-400 dark:hover:bg-neutral-800",
              ].join(" ")
            }
          >
            {source.logoUrl && (
              <img
                src={source.logoUrl}
                alt=""
                loading="lazy"
                onError={(event) => {
                  event.currentTarget.style.visibility = "hidden";
                }}
                className="size-4 flex-none rounded-sm object-contain"
              />
            )}
            <span className="truncate">{source.name}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
