import { EmptyIcon, OfflineIcon } from "./icons";

const SKELETON_KEYS = ["a", "b", "c", "d"] as const;
const SKELETON_WIDTHS = ["92%", "74%", "86%", "66%"] as const;

const DASHED_BLOCK =
  "flex-none rounded-xl border border-dashed border-neutral-300 bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-900";

/** 실제 카드(제목 2줄 + 메타 1줄)와 같은 높이라 로드 시 목록이 튀지 않는다. */
function CardSkeleton({ titleWidth }: { titleWidth: string }) {
  return (
    <div className="flex-none rounded-xl border border-dashed border-neutral-200 bg-neutral-50 px-4 py-3.5 lg:py-4 dark:border-neutral-800 dark:bg-neutral-900">
      <div className="h-[15px] animate-pulse rounded bg-neutral-200 lg:h-4 dark:bg-neutral-800" />
      <div
        className="mt-2 h-[15px] animate-pulse rounded bg-neutral-200 lg:h-4 dark:bg-neutral-800"
        style={{ width: titleWidth }}
      />
      <div className="mt-2.5 h-3 w-32 animate-pulse rounded bg-neutral-200 dark:bg-neutral-800" />
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
      <EmptyIcon className="size-20 text-neutral-300 dark:text-neutral-700" />
      <p className="text-lg text-neutral-600 dark:text-neutral-300">아직 수집된 글이 없어요</p>
      <p className="text-sm text-neutral-400 dark:text-neutral-500">
        {scoped ? "이 소스의 " : ""}새 글이 올라오면 여기에 표시됩니다
      </p>
    </div>
  );
}

/** 연결이 돌아오면 보류된 요청이 저절로 이어지므로 재시도 버튼을 두지 않는다. */
export function OfflineFeed() {
  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-3.5 px-6 text-center">
      <OfflineIcon className="size-20 text-neutral-300 dark:text-neutral-700" />
      <p className="text-lg text-neutral-600 dark:text-neutral-300">인터넷 연결이 끊겼어요</p>
      <p className="text-sm text-neutral-400 dark:text-neutral-500">
        다시 연결되면 자동으로 불러옵니다
      </p>
    </div>
  );
}

export function OfflineBlock() {
  return (
    <div className={`flex items-center justify-center gap-2 px-4 py-5 text-center ${DASHED_BLOCK}`}>
      <OfflineIcon className="size-4 flex-none text-neutral-400 dark:text-neutral-500" />
      <p className="text-sm text-neutral-600 dark:text-neutral-300">
        연결이 끊겼어요. 다시 연결되면 이어서 불러옵니다
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
    <div className={`flex flex-col items-center gap-3 px-4 py-5 text-center ${DASHED_BLOCK}`}>
      <p className="text-sm text-neutral-600 dark:text-neutral-300">{message}</p>
      <button
        type="button"
        onClick={onRetry}
        className="rounded-full border border-neutral-900 px-4 py-1.5 text-sm text-neutral-900 transition-colors hover:bg-neutral-900 hover:text-white dark:border-neutral-100 dark:text-neutral-100 dark:hover:bg-neutral-100 dark:hover:text-neutral-900"
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
