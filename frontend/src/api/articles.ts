import { apiGet } from "./client";

export type ArticleSource = {
  feed: string;
  source: string;
  sourceName: string;
};

export type Article = {
  id: number;
  title: string;
  url: string;
  summary: string | null;
  author: string | null;
  publishedAt: string;
  sources: ArticleSource[];
};

export type ArticlePage = {
  items: Article[];
  nextCursor: string | null;
};

export type ArticleQuery = {
  cursor?: string | null;
  source?: string;
};

export function fetchArticles({ cursor, source }: ArticleQuery = {}): Promise<ArticlePage> {
  return apiGet<ArticlePage>("/api/articles", {
    cursor: cursor ?? undefined,
    source,
  });
}
