import type { Article } from "../api/articles";
import { formatPublishedAt } from "../lib/publishedAt";
import { FOCUS_RING } from "../lib/styles";
import { ExternalLinkIcon } from "./icons";
import { SourceMark } from "./SourceMark";

type Props = {
  article: Article;
  showSourceName: boolean;
  /** 소스 slug로 찾은 로고. 소스 페이지에서는 제목줄이 대신 들고 있어 넘기지 않는다. */
  logoUrl?: string | null;
};

export function ArticleCard({ article, showSourceName, logoUrl }: Props) {
  const publishedAt = formatPublishedAt(article.publishedAt);
  // 같은 글이 여러 소스에서 수집될 수 있어 이름을 모두 적는다.
  const sourceNames = article.sources.map((source) => source.sourceName);
  // author에 블로그명을 그대로 넣는 피드가 있다. 소스 이름과 같으면 덧붙일 정보가 없다.
  const author =
    article.author !== null && sourceNames.includes(article.author) ? null : article.author;
  // 누가 언제 썼는지를 한 줄에 모은다. 줄을 나누면 카드만 길어지고 훑는 데는 보탬이 없다.
  const trailing = [author, publishedAt].filter(Boolean).join(" · ");

  return (
    <a
      href={article.url}
      target="_blank"
      rel="noreferrer"
      className={`group relative block flex-none rounded-2xl border border-line bg-surface p-4 shadow-raise transition-[background-color,border-color,box-shadow,translate] hover:-translate-y-px hover:border-line-strong hover:bg-surface-lift hover:shadow-raise-hover lg:px-5 lg:py-[1.125rem] ${FOCUS_RING}`}
    >
      {/* 표지 이미지가 없는 피드라 로고가 카드의 얼굴 노릇을 한다. */}
      {/* 날짜가 YYYY.MM.DD 고정폭이라 tabular-nums를 주면 세로로 자릿수가 맞는다. */}
      <div className="mb-2 flex items-center gap-2 pr-7 text-meta tabular-nums">
        {showSourceName && (
          <>
            <SourceMark src={logoUrl} className="size-[18px]" />
            <span className="truncate font-medium text-ink-muted">{sourceNames.join(", ")}</span>
          </>
        )}
        {showSourceName && trailing && (
          <span aria-hidden="true" className="-mx-1 flex-none text-ink-faint/50">
            ·
          </span>
        )}
        {trailing && <span className="truncate text-ink-faint">{trailing}</span>}
      </div>

      <h3 className="line-clamp-2 text-title font-semibold text-ink">{article.title}</h3>

      {article.summary && (
        // 요약이 없는 피드는 본문 전체가 넘어오기도 해서 줄 수를 묶어 둔다.
        <p className="mt-2 line-clamp-2 text-body text-ink-muted">{article.summary}</p>
      )}

      {/* 카드가 모두 바깥 링크라 늘 켜 두면 알려 주는 게 없다. 누르려는 순간에만 나타난다. */}
      <ExternalLinkIcon className="absolute right-4 top-4 size-3.5 text-ink-faint opacity-0 transition-opacity group-hover:opacity-100 group-focus-visible:opacity-100 lg:right-5 lg:top-5" />
    </a>
  );
}
