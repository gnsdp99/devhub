type Props = {
  src: string | null | undefined;
  /** 크기와 상태별 처리를 부르는 쪽에서 정한다. */
  className?: string;
};

/** 소스를 알아보게 하는 로고. 사이드바·카드·제목줄이 같은 것을 쓴다. */
export function SourceMark({ src, className = "" }: Props) {
  if (!src) {
    return null;
  }

  return (
    <img
      src={src}
      alt=""
      loading="lazy"
      onError={(event) => {
        event.currentTarget.style.visibility = "hidden";
      }}
      className={`flex-none rounded-md object-contain ${className}`}
    />
  );
}
