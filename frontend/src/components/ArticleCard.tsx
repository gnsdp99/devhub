import type { Article } from "../api/articles";
import { formatPublishedAt } from "../lib/publishedAt";
import { ExternalLinkIcon } from "./icons";

type Props = {
  article: Article;
  showSourceName: boolean;
};

export function ArticleCard({ article, showSourceName }: Props) {
  const publishedAt = formatPublishedAt(article.publishedAt);
  // 같은 글이 여러 소스에서 수집될 수 있어 이름을 모두 적는다.
  const sourceNames = article.sources.map((source) => source.sourceName);
  // author에 블로그명을 그대로 넣는 피드가 있다. 소스 이름과 같으면 덧붙일 정보가 없다.
  const author =
    article.author !== null && sourceNames.includes(article.author) ? null : article.author;
  const meta = [showSourceName ? sourceNames.join(", ") : null, author, publishedAt]
    .filter(Boolean)
    .join(" · ");

  return (
    <a
      href={article.url}
      target="_blank"
      rel="noreferrer"
      className="relative block flex-none rounded-xl border border-neutral-200 bg-white py-3.5 pl-4 pr-10 transition-colors hover:border-neutral-400 hover:bg-neutral-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-neutral-900 lg:py-4"
    >
      <h3 className="line-clamp-2 text-[15px] font-medium leading-snug text-neutral-900 lg:text-base">
        {article.title}
      </h3>
      {article.summary && (
        // 요약이 없는 피드는 본문 전체가 넘어오기도 해서 줄 수를 묶어 둔다.
        <p className="mt-1.5 line-clamp-2 text-[13px] leading-relaxed text-neutral-600 lg:text-sm">
          {article.summary}
        </p>
      )}
      <p className="mt-1.5 truncate text-xs text-neutral-500 lg:text-[13px]">{meta}</p>
      <ExternalLinkIcon className="absolute right-3 top-3.5 size-4 text-neutral-300" />
    </a>
  );
}
