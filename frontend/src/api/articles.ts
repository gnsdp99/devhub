const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export type Article = {
  id: number;
  title: string;
  url: string;
  summary: string | null;
  publishedAt: string;
  feed: string;
  source: string;
  sourceName: string;
};

export type ArticlePage = {
  items: Article[];
  nextCursor: string | null;
};

export async function fetchArticles(): Promise<ArticlePage> {
  const response = await fetch(`${BASE_URL}/api/articles`);
  if (!response.ok) {
    throw new Error(`기사 목록을 불러오지 못했습니다 (${response.status})`);
  }
  return response.json();
}
