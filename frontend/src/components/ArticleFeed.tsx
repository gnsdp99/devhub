import { useInfiniteQuery } from "@tanstack/react-query";
import { useEffect, useRef } from "react";
import { fetchArticles } from "../api/articles";
import { ArticleCard } from "./ArticleCard";
import {
  CardSkeletons,
  EmptyFeed,
  FeedErrorBlock,
  FeedErrorState,
  OfflineBlock,
  OfflineFeed,
} from "./FeedStates";

type Props = {
  /** 소스 slug. 전체 피드면 undefined. */
  source?: string;
  showSourceName: boolean;
};

export function ArticleFeed({ source, showSourceName }: Props) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const {
    data,
    error,
    status,
    fetchStatus,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    refetch,
  } = useInfiniteQuery({
    queryKey: ["articles", source ?? null],
    queryFn: ({ pageParam }) => fetchArticles({ cursor: pageParam, source }),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => lastPage.nextCursor,
  });

  // 연결이 끊기면 react-query가 요청을 보류한다. 스켈레톤을 계속 두면 영영 로딩처럼 보인다.
  const offline = fetchStatus === "paused";
  // 실패했거나 끊긴 상태에서는 관찰을 멈춰야 요청이 쌓이지 않는다.
  const observing = hasNextPage && !isFetchingNextPage && error === null && !offline;

  useEffect(() => {
    const root = scrollRef.current;
    const sentinel = sentinelRef.current;
    if (!root || !sentinel || !observing) {
      return;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          fetchNextPage();
        }
      },
      { root, rootMargin: "400px" },
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [observing, fetchNextPage]);

  const articles = data?.pages.flatMap((page) => page.items) ?? [];

  return (
    // 스크롤은 폭 전체가 받고, 읽는 폭만 안쪽에서 묶는다. 사이드바를 접어도 한 줄 길이가 그대로다.
    <div ref={scrollRef} className="flex-1 overflow-y-auto p-3 lg:p-4">
      <div className="mx-auto flex min-h-full w-full max-w-3xl flex-col gap-2.5 lg:gap-3">
        {status === "pending" && (offline ? <OfflineFeed /> : <CardSkeletons count={4} />)}

        {status !== "pending" && data === undefined && (
          // 서버 detail은 ApiError에 담겨 콘솔로만 가고, 화면 문구는 여기서 정한다.
          <FeedErrorState message="글을 불러오지 못했어요" onRetry={() => refetch()} />
        )}

        {data !== undefined && articles.length === 0 && <EmptyFeed scoped={source !== undefined} />}

        {articles.map((article) => (
          <ArticleCard key={article.id} article={article} showSourceName={showSourceName} />
        ))}

        {isFetchingNextPage && <CardSkeletons count={2} />}

        {data !== undefined && error !== null && (
          <FeedErrorBlock message="다음 글을 불러오지 못했어요" onRetry={() => fetchNextPage()} />
        )}

        {data !== undefined && offline && <OfflineBlock />}

        <div ref={sentinelRef} className="h-px flex-none" />
      </div>
    </div>
  );
}
