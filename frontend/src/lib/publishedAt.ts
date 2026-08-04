/** 작성일은 어느 화면에서나 YYYY.MM.DD로 적는다. 값이 깨졌으면 빈 문자열이라 메타 줄에서 빠진다. */
export function formatPublishedAt(publishedAt: string): string {
  const published = new Date(publishedAt);
  if (Number.isNaN(published.getTime())) {
    return "";
  }

  const month = `${published.getMonth() + 1}`.padStart(2, "0");
  const day = `${published.getDate()}`.padStart(2, "0");
  return `${published.getFullYear()}.${month}.${day}`;
}
