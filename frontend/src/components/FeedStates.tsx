import { OUTLINE_BUTTON } from "../lib/styles";
import { EmptyIcon, OfflineIcon } from "./icons";

const SKELETON_KEYS = ["a", "b", "c", "d"] as const;
const SKELETON_WIDTHS = ["92%", "74%", "86%", "66%"] as const;

/** 실패와 끊김만 파선을 쓴다. 로딩은 실제 카드와 같은 실선이라 둘이 헷갈리지 않는다. */
const DASHED_BLOCK =
  "flex-none rounded-2xl border border-dashed border-line-strong bg-panel text-center";

/** 실제 카드(소스 줄 + 제목 2줄 + 메타 1줄)와 같은 높이라 로드 시 목록이 튀지 않는다. */
function CardSkeleton({ titleWidth }: { titleWidth: string }) {
  return (
    <div
      className="flex-none rounded-2xl border border-line bg-surface p-4 shadow-raise lg:px-5 lg:py-[1.125rem]"
      aria-hidden="true"
    >
      <div className="mb-2 flex items-center gap-2">
        <div className="size-[18px] animate-pulse rounded-md bg-line" />
        <div className="h-3 w-32 animate-pulse rounded-sm bg-line" />
      </div>
      <div className="h-4 animate-pulse rounded-sm bg-line" />
      <div className="mt-2 h-4 animate-pulse rounded-sm bg-line" style={{ width: titleWidth }} />
      <div className="mt-3.5 h-3 w-full animate-pulse rounded-sm bg-line" />
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
      <EmptyIcon className="size-20 text-line-strong" />
      <p className="text-heading text-ink-muted">아직 수집된 글이 없어요</p>
      <p className="text-body text-ink-faint">
        {scoped ? "이 소스의 " : ""}새 글이 올라오면 여기에 표시됩니다
      </p>
    </div>
  );
}

/** 연결이 돌아오면 보류된 요청이 저절로 이어지므로 재시도 버튼을 두지 않는다. */
export function OfflineFeed() {
  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-3.5 px-6 text-center">
      <OfflineIcon className="size-20 text-line-strong" />
      <p className="text-heading text-ink-muted">인터넷 연결이 끊겼어요</p>
      <p className="text-body text-ink-faint">다시 연결되면 자동으로 불러옵니다</p>
    </div>
  );
}

export function OfflineBlock() {
  return (
    <div className={`flex items-center justify-center gap-2 px-4 py-5 ${DASHED_BLOCK}`}>
      <OfflineIcon className="size-4 flex-none text-ink-faint" />
      <p className="text-body text-ink-muted">연결이 끊겼어요. 다시 연결되면 이어서 불러옵니다</p>
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
    <div className={`flex flex-col items-center gap-3 px-4 py-5 ${DASHED_BLOCK}`}>
      <p className="text-body text-ink-muted">{message}</p>
      <button type="button" onClick={onRetry} className={OUTLINE_BUTTON}>
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
