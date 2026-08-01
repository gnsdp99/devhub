import { useCallback, useState, useSyncExternalStore } from "react";

export function useMediaQuery(query: string): boolean {
  const [list] = useState(() => window.matchMedia(query));

  const subscribe = useCallback(
    (onChange: () => void) => {
      list.addEventListener("change", onChange);
      return () => list.removeEventListener("change", onChange);
    },
    [list],
  );

  return useSyncExternalStore(subscribe, () => list.matches);
}
