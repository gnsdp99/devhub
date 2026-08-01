import { EmptyIcon } from "./icons";

const SKELETON_KEYS = ["a", "b", "c", "d"] as const;
const SKELETON_WIDTHS = ["92%", "74%", "86%", "66%"] as const;

/** 실제 카드(제목 2줄 + 메타 1줄)와 같은 높이라 로드 시 목록이 튀지 않는다. */
function CardSkeleton({ titleWidth }: { titleWidth: string }) {
  return (
    <div className="flex-none rounded-xl border border-dashed border-neutral-200 bg-neutral-50 px-4 py-3.5 lg:py-4">
      <div className="h-[15px] animate-pulse rounded bg-neutral-200 lg:h-4" />
      <div
        className="mt-2 h-[15px] animate-pulse rounded bg-neutral-200 lg:h-4"
        style={{ width: titleWidth }}
      />
      <div className="mt-2.5 h-3 w-32 animate-pulse rounded bg-neutral-200" />
    </div>
  );
}

export function CardSkeletons({ count }: { count: number }) {
  return (
    <>
      {SKELETON_KEYS.slice(0, count).map((key, index) => (
        <CardSkeleton key={key} titleWidth={SKELETON_WIDTHS[index]} />
      ))}
    </>
  );
}

export function EmptyFeed({ scoped }: { scoped: boolean }) {
  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-3.5 px-6 text-center">
      <EmptyIcon className="size-20 text-neutral-300" />
      <p className="text-lg text-neutral-600">아직 수집된 글이 없어요</p>
      <p className="text-sm text-neutral-400">
        {scoped ? "이 소스의 " : ""}새 글이 올라오면 여기에 표시됩니다
      </p>
    </div>
  );
}

type ErrorProps = {
  message: string;
  onRetry: () => void;
};

/** 목록 하단 실패 블록 — 이미 불러온 카드는 그대로 두고 스켈레톤 자리만 대체한다. */
export function FeedErrorBlock({ message, onRetry }: ErrorProps) {
  return (
    <div className="flex flex-none flex-col items-center gap-3 rounded-xl border border-dashed border-neutral-300 bg-neutral-50 px-4 py-5 text-center">
      <p className="text-sm text-neutral-600">{message}</p>
      <button
        type="button"
        onClick={onRetry}
        className="rounded-full border border-neutral-900 px-4 py-1.5 text-sm text-neutral-900 transition-colors hover:bg-neutral-900 hover:text-white"
      >
        다시 시도
      </button>
    </div>
  );
}

export function FeedErrorState({ message, onRetry }: ErrorProps) {
  return (
    <div className="flex flex-1 items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <FeedErrorBlock message={message} onRetry={onRetry} />
      </div>
    </div>
  );
}
