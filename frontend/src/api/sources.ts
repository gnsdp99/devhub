import { apiGet } from "./client";

export type Source = {
  slug: string;
  name: string;
  category: string;
  siteUrl: string;
};

export function fetchSources(): Promise<Source[]> {
  return apiGet<Source[]>("/api/sources");
}
