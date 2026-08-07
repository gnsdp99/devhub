/** 누를 수 있는 것은 모두 같은 초점 표시를 쓴다. 배경이 무엇이든 2px 잉크 링이다. */
export const FOCUS_RING =
  "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink";

/** 테두리 없이 호버 배경만으로 누를 수 있음을 알린다. */
export const ICON_BUTTON = `flex size-8 flex-none items-center justify-center rounded-lg text-ink-muted transition-colors hover:bg-tint hover:text-ink ${FOCUS_RING}`;

/** 막다른 화면에서 되돌아 나가는 단 하나의 버튼. 호버하면 색이 뒤집힌다. */
export const OUTLINE_BUTTON = `rounded-lg border border-ink px-4 py-1.5 text-meta font-medium text-ink transition-colors hover:bg-ink hover:text-canvas ${FOCUS_RING}`;
