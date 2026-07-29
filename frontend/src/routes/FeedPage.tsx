import { useQuery } from "@tanstack/react-query";
import { fetchArticles } from "../api/articles";

export function FeedPage() {
  const { data, isPending, error } = useQuery({
    queryKey: ["articles"],
    queryFn: fetchArticles,
  });

  if (isPending) {
    return <p className="p-8">불러오는 중</p>;
  }

  if (error) {
    return <p className="p-8">{error.message}</p>;
  }

  return (
    <ul className="p-8">
      {data.items.map((article) => (
        <li key={article.id}>
          <a href={article.url}>{article.title}</a>
          <span className="ml-2 text-sm text-gray-500">{article.sourceName}</span>
        </li>
      ))}
    </ul>
  );
}
