/**
 * 응답이 너무 빨리 오면 스켈레톤이 한 프레임 스쳤다 사라져 깜빡임으로만 보인다.
 * 최소 시간을 채워 스켈레톤이 눈에 남게 한다. 느린 응답에는 아무 지연도 더하지 않고,
 * 실패는 곧바로 던져 재시도 블록이 지체 없이 뜬다.
 */
export async function withMinDuration<T>(work: Promise<T>, ms: number): Promise<T> {
  const [value] = await Promise.all([work, new Promise((resolve) => setTimeout(resolve, ms))]);
  return value;
}
