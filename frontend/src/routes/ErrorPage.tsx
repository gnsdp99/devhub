import { useEffect } from "react";
import { useRouteError } from "react-router";
import { OUTLINE_BUTTON } from "../lib/styles";

export function ErrorPage() {
  const error = useRouteError();

  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="flex h-dvh flex-col items-center justify-center gap-3.5 bg-canvas px-6 text-center text-ink">
      <p className="text-heading text-ink-muted">화면을 표시하지 못했어요</p>
      <p className="text-body text-ink-faint">새로고침해도 같은 문제가 이어지면 알려 주세요</p>
      <button type="button" onClick={() => window.location.reload()} className={OUTLINE_BUTTON}>
        새로고침
      </button>
    </div>
  );
}
