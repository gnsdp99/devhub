import { useEffect } from "react";

const SITE = "DevHub";

/** 전체 피드는 사이트 이름만, 소스처럼 대상이 있는 화면은 "대상 | DevHub". */
export function useDocumentTitle(title = "") {
  useEffect(() => {
    document.title = title ? `${title} | ${SITE}` : SITE;
  }, [title]);
}
