export type AnalyticsParams = Record<string, string | number | boolean | undefined>;

/** No-op analytics hook; a real provider can be wired in later. */
export function trackEvent(name: string, params?: AnalyticsParams): void {
  void name;
  void params;
}
