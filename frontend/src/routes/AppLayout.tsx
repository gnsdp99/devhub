import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Outlet, useOutletContext } from "react-router";
import { fetchSources, type Source } from "../api/sources";
import { Sidebar } from "../components/Sidebar";
import { TopBar } from "../components/TopBar";
import { useMediaQuery } from "../lib/useMediaQuery";
import { useTheme } from "../lib/useTheme";

export type AppLayoutContext = {
  sources: Source[] | undefined;
};

export function useAppLayout(): AppLayoutContext {
  return useOutletContext<AppLayoutContext>();
}

function sidebarMessage(offline: boolean, failed: boolean): string | undefined {
  if (offline) {
    return "인터넷 연결이 끊겼어요";
  }
  if (failed) {
    return "소스 목록을 불러오지 못했어요";
  }
  return undefined;
}

export function AppLayout() {
  const { theme, toggle: toggleTheme } = useTheme();
  const isDesktop = useMediaQuery("(min-width: 1024px)");
  const [desktopOpen, setDesktopOpen] = useState(true);
  const [drawerOpen, setDrawerOpen] = useState(false);
  // 사이드바 검색어는 소스를 옮겨 다녀도 유지된다.
  const [sourceQuery, setSourceQuery] = useState("");

  const sources = useQuery({
    queryKey: ["sources"],
    queryFn: fetchSources,
    staleTime: 5 * 60_000,
  });
  const sourcesOffline = sources.fetchStatus === "paused";

  function toggleSidebar() {
    if (isDesktop) {
      setDesktopOpen((open) => !open);
    } else {
      setDrawerOpen((open) => !open);
    }
  }

  // 사이드바 안의 닫기 버튼. 데스크톱은 칸을 접고, 모바일은 드로어를 밀어 넣는다.
  function closeSidebar() {
    if (isDesktop) {
      setDesktopOpen(false);
    } else {
      setDrawerOpen(false);
    }
  }

  const context: AppLayoutContext = { sources: sources.data };

  return (
    <div className="flex h-dvh flex-col overflow-hidden bg-white text-neutral-900 dark:bg-neutral-950 dark:text-neutral-100">
      <TopBar
        sidebarOpen={isDesktop ? desktopOpen : drawerOpen}
        onMenuClick={toggleSidebar}
        theme={theme}
        onThemeToggle={toggleTheme}
      />

      {/* 드로어와 덮개는 이 칸을 기준으로 잡혀서 헤더바를 가리지 않는다. */}
      <div className="relative flex min-h-0 flex-1">
        {drawerOpen && (
          <button
            type="button"
            onClick={() => setDrawerOpen(false)}
            aria-label="사이드바 닫기"
            className="absolute inset-0 z-30 cursor-default bg-neutral-900/50 lg:hidden"
          />
        )}

        <Sidebar
          sources={sources.data}
          isPending={sources.isPending && !sourcesOffline}
          errorMessage={sidebarMessage(sourcesOffline, sources.error !== null)}
          query={sourceQuery}
          onQueryChange={setSourceQuery}
          drawerOpen={drawerOpen}
          desktopOpen={desktopOpen}
          onClose={closeSidebar}
          onSelect={() => setDrawerOpen(false)}
        />

        <main className="flex min-w-0 flex-1 flex-col">
          <Outlet context={context} />
        </main>
      </div>
    </div>
  );
}
