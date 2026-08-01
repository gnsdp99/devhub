import { useInfiniteQuery } from "@tanstack/react-query";
import { useEffect, useRef } from "react";
import { fetchArticles } from "../api/articles";
import { ArticleCard } from "./ArticleCard";
import { CardSkeletons, EmptyFeed, FeedErrorBlock, FeedErrorState } from "./FeedStates";

type Props = {
  /** 소스 slug. 전체 피드면 undefined. */
  source?: string;
  showSourceName: boolean;
};

export function ArticleFeed({ source, showSourceName }: Props) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const { data, error, status, hasNextPage, isFetchingNextPage, fetchNextPage, refetch } =
    useInfiniteQuery({
      queryKey: ["articles", source ?? null],
      queryFn: ({ pageParam }) => fetchArticles({ cursor: pageParam, source }),
      initialPageParam: null as string | null,
      getNextPageParam: (lastPage) => lastPage.nextCursor,
    });

  // 실패 상태에서는 관찰을 멈춰야 "다시 시도" 없이 요청이 반복되지 않는다.
  const paused = !hasNextPage || isFetchingNextPage || error !== null;

  useEffect(() => {
    const root = scrollRef.current;
    const sentinel = sentinelRef.current;
    if (!root || !sentinel || paused) {
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
  }, [paused, fetchNextPage]);

  const articles = data?.pages.flatMap((page) => page.items) ?? [];

  return (
    <div
      ref={scrollRef}
      className="flex flex-1 flex-col gap-2.5 overflow-y-auto p-3 lg:gap-3 lg:p-4"
    >
      {status === "pending" && <CardSkeletons count={4} />}

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

      <div ref={sentinelRef} className="h-px flex-none" />
    </div>
  );
}
