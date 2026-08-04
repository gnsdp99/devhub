type IconProps = {
  className?: string;
};

export function ExternalLinkIcon({ className }: IconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 16 16"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      <path d="M6 3h7v7" />
      <path d="M13 3 3.5 12.5" />
    </svg>
  );
}

export function MenuIcon({ className }: IconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      aria-hidden="true"
      focusable="false"
    >
      <path d="M4 6h12M4 10h12M4 14h12" />
    </svg>
  );
}

export function ChevronLeftIcon({ className }: IconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      <path d="M12.5 4.5 7 10l5.5 5.5" />
    </svg>
  );
}

export function SunIcon({ className }: IconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      aria-hidden="true"
      focusable="false"
    >
      <circle cx="10" cy="10" r="3.5" />
      <path d="M10 2v1.5M10 16.5V18M2 10h1.5M16.5 10H18M4.4 4.4l1 1M14.6 14.6l1 1M15.6 4.4l-1 1M5.4 14.6l-1 1" />
    </svg>
  );
}

export function MoonIcon({ className }: IconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      <path d="M16.5 12.4A7 7 0 0 1 7.6 3.5a7 7 0 1 0 8.9 8.9Z" />
    </svg>
  );
}

export function OfflineIcon({ className }: IconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 48 48"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      aria-hidden="true"
      focusable="false"
    >
      <path d="M5 17a29 29 0 0 1 38 0" />
      <path d="M13 26a18 18 0 0 1 22 0" />
      <path d="M20 34a8 8 0 0 1 8 0" />
      <path d="M24 41h.01" />
      <path d="M7 7 41 41" />
    </svg>
  );
}

export function EmptyIcon({ className }: IconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 48 48"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      aria-hidden="true"
      focusable="false"
    >
      <circle cx="24" cy="24" r="23" strokeDasharray="5 5" />
      <circle cx="24" cy="24" r="7" />
    </svg>
  );
}
