import { useEffect } from "react";
import { useRouteError } from "react-router";

export function ErrorPage() {
  const error = useRouteError();

  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="flex h-dvh flex-col items-center justify-center gap-3.5 bg-white px-6 text-center text-neutral-900 dark:bg-neutral-950 dark:text-neutral-100">
      <p className="text-lg text-neutral-600 dark:text-neutral-300">화면을 표시하지 못했어요</p>
      <p className="text-sm text-neutral-400 dark:text-neutral-500">
        새로고침해도 같은 문제가 이어지면 알려 주세요
      </p>
      <button
        type="button"
        onClick={() => window.location.reload()}
        className="mt-1 rounded-full border border-neutral-900 px-4 py-1.5 text-sm text-neutral-900 transition-colors hover:bg-neutral-900 hover:text-white dark:border-neutral-100 dark:text-neutral-100 dark:hover:bg-neutral-100 dark:hover:text-neutral-900"
      >
        새로고침
      </button>
    </div>
  );
}
