import { useCallback, useEffect, useState } from "react";

export type Theme = "light" | "dark";

const STORAGE_KEY = "devhub-theme";
const QUERY = "(prefers-color-scheme: dark)";

function storedTheme(): Theme | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY);
    return value === "light" || value === "dark" ? value : null;
  } catch {
    return null;
  }
}

export function useTheme() {
  // index.html의 선행 스크립트가 이미 정해 둔 값에서 출발한다.
  const [theme, setTheme] = useState<Theme>(() =>
    document.documentElement.dataset.theme === "dark" ? "dark" : "light",
  );

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
  }, [theme]);

  useEffect(() => {
    const media = window.matchMedia(QUERY);
    // 직접 고른 적이 없을 때만 OS 설정을 따라간다.
    const sync = () => {
      if (storedTheme() === null) {
        setTheme(media.matches ? "dark" : "light");
      }
    };
    media.addEventListener("change", sync);
    return () => media.removeEventListener("change", sync);
  }, []);

  const toggle = useCallback(() => {
    setTheme((current) => {
      const next = current === "dark" ? "light" : "dark";
      try {
        localStorage.setItem(STORAGE_KEY, next);
      } catch {
        // 저장에 실패해도 이번 방문에는 적용된다.
      }
      return next;
    });
  }, []);

  return { theme, toggle };
}
