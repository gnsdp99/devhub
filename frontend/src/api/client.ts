const BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? "";

type ProblemDetail = {
  title?: string;
  detail?: string;
};

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export async function apiGet<T>(
  path: string,
  params: Record<string, string | number | undefined> = {},
): Promise<T> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined) {
      query.set(key, String(value));
    }
  }
  const queryString = query.toString();
  const response = await fetch(`${BASE_URL}${path}${queryString ? `?${queryString}` : ""}`);
  if (!response.ok) {
    throw new ApiError(response.status, await messageOf(response));
  }
  return (await response.json()) as T;
}

async function messageOf(response: Response): Promise<string> {
  const fallback = `요청이 실패했습니다 (${response.status})`;
  try {
    const problem = (await response.json()) as ProblemDetail;
    return problem.detail ?? problem.title ?? fallback;
  } catch {
    return fallback;
  }
}
