import { apiGet } from "./client";

export type Source = {
  slug: string;
  name: string;
  siteUrl: string;
  logoUrl: string | null;
};

export function fetchSources(): Promise<Source[]> {
  return apiGet<Source[]>("/api/sources");
}
