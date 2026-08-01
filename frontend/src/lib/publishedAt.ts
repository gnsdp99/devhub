const MINUTE = 60_000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;
const WEEK = 7 * DAY;

const absolute = new Intl.DateTimeFormat("ko-KR", {
  year: "numeric",
  month: "long",
  day: "numeric",
});

/** 7일 이내면 상대 표기("3시간 전"), 그보다 오래됐으면 절대 표기("2026년 6월 14일"). */
export function formatPublishedAt(publishedAt: string, now: number = Date.now()): string {
  const published = new Date(publishedAt);
  const elapsed = now - published.getTime();

  if (Number.isNaN(elapsed) || elapsed < 0 || elapsed >= WEEK) {
    return absolute.format(published);
  }
  if (elapsed < MINUTE) {
    return "방금 전";
  }
  if (elapsed < HOUR) {
    return `${Math.floor(elapsed / MINUTE)}분 전`;
  }
  if (elapsed < DAY) {
    return `${Math.floor(elapsed / HOUR)}시간 전`;
  }
  return `${Math.floor(elapsed / DAY)}일 전`;
}
