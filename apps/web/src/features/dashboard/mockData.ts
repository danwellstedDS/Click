// ─── Mock data for Portfolio Intelligence Dashboard ───────────────────────────

export interface Property {
  id: string;
  name: string;
  region: string;
  spend: number;
  revenue: number;
  roas: number;
  marginalRoas: number;
  headroom: number; // 0–100, higher = more room to grow
  otaDependency: number; // % of revenue from OTA
}

export const PROPERTIES: Property[] = [
  { id: "p1", name: "The Grand Mayfair",   region: "London",       spend: 42000,  revenue: 189000, roas: 4.5, marginalRoas: 2.1, headroom: 72, otaDependency: 38 },
  { id: "p2", name: "Harbour View Dubai",  region: "Middle East",  spend: 61000,  revenue: 244000, roas: 4.0, marginalRoas: 1.8, headroom: 31, otaDependency: 52 },
  { id: "p3", name: "Park Lane Residences",region: "London",       spend: 28000,  revenue: 98000,  roas: 3.5, marginalRoas: 3.2, headroom: 85, otaDependency: 29 },
  { id: "p4", name: "Marina Bay Suites",   region: "Asia Pacific", spend: 54000,  revenue: 189000, roas: 3.5, marginalRoas: 2.8, headroom: 58, otaDependency: 45 },
  { id: "p5", name: "Costa del Sol Resort",region: "Europe",       spend: 19000,  revenue: 57000,  roas: 3.0, marginalRoas: 3.8, headroom: 91, otaDependency: 61 },
  { id: "p6", name: "Alpine Lodge Zermatt",region: "Europe",       spend: 11000,  revenue: 28600,  roas: 2.6, marginalRoas: 4.1, headroom: 95, otaDependency: 33 },
  { id: "p7", name: "NYC Midtown Tower",   region: "North America",spend: 73000,  revenue: 219000, roas: 3.0, marginalRoas: 1.4, headroom: 12, otaDependency: 41 },
  { id: "p8", name: "Sydney Harbourside",  region: "Asia Pacific", spend: 33000,  revenue: 99000,  roas: 3.0, marginalRoas: 2.9, headroom: 63, otaDependency: 48 },
];

// ─── Revenue trend (last 12 weeks) ───────────────────────────────────────────

export interface WeeklyRevenue {
  week: string;
  revenue: number;
  baseline: number;
}

export const REVENUE_TREND: WeeklyRevenue[] = [
  { week: "W1",  revenue: 842000,  baseline: 780000 },
  { week: "W2",  revenue: 891000,  baseline: 785000 },
  { week: "W3",  revenue: 875000,  baseline: 790000 },
  { week: "W4",  revenue: 924000,  baseline: 795000 },
  { week: "W5",  revenue: 968000,  baseline: 800000 },
  { week: "W6",  revenue: 1012000, baseline: 805000 },
  { week: "W7",  revenue: 987000,  baseline: 810000 },
  { week: "W8",  revenue: 1043000, baseline: 815000 },
  { week: "W9",  revenue: 1098000, baseline: 820000 },
  { week: "W10", revenue: 1076000, baseline: 825000 },
  { week: "W11", revenue: 1124000, baseline: 830000 },
  { week: "W12", revenue: 1187000, baseline: 835000 },
];

// ─── RevPAR uplift per property ───────────────────────────────────────────────

export interface RevParUplift {
  name: string;
  uplift: number;
}

export const REVPAR_UPLIFT: RevParUplift[] = PROPERTIES.map((p) => ({
  name: p.name.split(" ").slice(0, 2).join(" "),
  uplift: parseFloat(((p.revenue / p.spend / 4 - 1) * 100).toFixed(1)),
}));

// ─── Channel mix ─────────────────────────────────────────────────────────────

export interface ChannelMix {
  name: string;
  value: number;
}

export const CHANNEL_MIX: ChannelMix[] = [
  { name: "Direct", value: 54 },
  { name: "OTA",    value: 32 },
  { name: "GDS",    value: 9  },
  { name: "Other",  value: 5  },
];

// ─── Spend trend per property (stacked area) ─────────────────────────────────

export interface SpendWeek {
  week: string;
  [propertyName: string]: string | number;
}

export const SPEND_TREND: SpendWeek[] = (() => {
  const weeks = ["W1","W2","W3","W4","W5","W6","W7","W8","W9","W10","W11","W12"];
  const seeds: Record<string, number> = {
    "Grand Mayfair":   38000,
    "Harbour Dubai":   55000,
    "Park Lane":       24000,
    "Marina Bay":      49000,
    "Costa Sol":       16000,
    "Alpine Zermatt":  9000,
    "NYC Midtown":     67000,
    "Sydney":          29000,
  };
  return weeks.map((week, i) => {
    const row: SpendWeek = { week };
    Object.entries(seeds).forEach(([k, base]) => {
      row[k] = Math.round(base * (1 + i * 0.015 + (Math.random() * 0.06 - 0.03)));
    });
    return row;
  });
})();

export const SPEND_TREND_KEYS = [
  "Grand Mayfair","Harbour Dubai","Park Lane","Marina Bay",
  "Costa Sol","Alpine Zermatt","NYC Midtown","Sydney",
];

// ─── Marginal ROAS / diminishing returns curve ───────────────────────────────

export interface MarginalPoint {
  spend: number;
  revenue: number;
  marginalRoas: number;
}

export const MARGINAL_ROAS: MarginalPoint[] = [
  { spend: 10,  revenue: 55,  marginalRoas: 5.5 },
  { spend: 20,  revenue: 98,  marginalRoas: 4.3 },
  { spend: 30,  revenue: 132, marginalRoas: 3.4 },
  { spend: 40,  revenue: 160, marginalRoas: 2.8 },
  { spend: 50,  revenue: 182, marginalRoas: 2.2 },
  { spend: 60,  revenue: 199, marginalRoas: 1.7 },
  { spend: 70,  revenue: 212, marginalRoas: 1.3 },
  { spend: 80,  revenue: 221, marginalRoas: 0.9 },
  { spend: 90,  revenue: 227, marginalRoas: 0.6 },
  { spend: 100, revenue: 231, marginalRoas: 0.4 },
];

// ─── CVR volatility (4 weeks, per property) ──────────────────────────────────

export interface CvrWeek {
  week: string;
  [key: string]: string | number;
}

export const CVR_VOLATILITY: CvrWeek[] = [
  { week: "W9",  mayfair: 3.2, dubai: 2.8, parkLane: 4.1, marina: 2.4 },
  { week: "W10", mayfair: 2.9, dubai: 3.1, parkLane: 3.8, marina: 2.9 },
  { week: "W11", mayfair: 3.5, dubai: 2.5, parkLane: 4.4, marina: 3.2 },
  { week: "W12", mayfair: 3.1, dubai: 3.4, parkLane: 3.9, marina: 2.7 },
];

// ─── Competitor share-of-voice ────────────────────────────────────────────────

export interface SovWeek {
  week: string;
  portfolio: number;
  competitor1: number;
  competitor2: number;
}

export const SOV_TREND: SovWeek[] = [
  { week: "W9",  portfolio: 34, competitor1: 28, competitor2: 22 },
  { week: "W10", portfolio: 36, competitor1: 27, competitor2: 23 },
  { week: "W11", portfolio: 35, competitor1: 29, competitor2: 21 },
  { week: "W12", portfolio: 38, competitor1: 26, competitor2: 22 },
];

// ─── Auction overlap matrix ───────────────────────────────────────────────────

export interface OverlapEntry {
  property: string;
  scores: Record<string, number>; // 0–100
}

const SHORT = ["Mayfair","Dubai","ParkLane","Marina","Costa","Alpine","NYC","Sydney"];

export const OVERLAP_MATRIX: OverlapEntry[] = SHORT.map((a, i) => ({
  property: a,
  scores: SHORT.reduce<Record<string, number>>((acc, b, j) => {
    if (i === j) { acc[b] = 0; return acc; }
    const base = Math.abs(i - j);
    acc[b] = Math.max(0, Math.min(100, 90 - base * 12 + Math.round(Math.random() * 20)));
    return acc;
  }, {}),
}));

export const OVERLAP_PROPERTY_NAMES = SHORT;

// ─── Internal CPC inflation ───────────────────────────────────────────────────

export interface CpcInflation {
  week: string;
  inflation: number;
}

export const CPC_INFLATION: CpcInflation[] = [
  { week: "W9",  inflation: 1.08 },
  { week: "W10", inflation: 1.11 },
  { week: "W11", inflation: 1.14 },
  { week: "W12", inflation: 1.18 },
];

// ─── Pace & forecast data ─────────────────────────────────────────────────────

export interface PaceRow {
  name: string;
  target: number;
  actual: number;
  pacing: number; // percentage actual/target
  status: "on-track" | "behind" | "at-risk";
}

export const PACE_DATA: PaceRow[] = PROPERTIES.map((p) => {
  const target = p.revenue * 1.05;
  const variance = (Math.random() - 0.4) * 0.2;
  const actual = target * (1 + variance);
  const pacing = (actual / target) * 100;
  const status: PaceRow["status"] =
    pacing >= 95 ? "on-track" : pacing >= 80 ? "at-risk" : "behind";
  return {
    name: p.name.split(" ").slice(0, 2).join(" "),
    target: Math.round(target),
    actual: Math.round(actual),
    pacing: Math.round(pacing),
    status,
  };
});

export interface ForecastPoint {
  week: string;
  actual: number | null;
  forecast: number;
  upper: number;
  lower: number;
}

export const FORECAST_BAND: ForecastPoint[] = [
  { week: "W9",  actual: 1076000, forecast: 1076000, upper: 1076000, lower: 1076000 },
  { week: "W10", actual: 1124000, forecast: 1124000, upper: 1124000, lower: 1124000 },
  { week: "W11", actual: 1187000, forecast: 1187000, upper: 1187000, lower: 1187000 },
  { week: "W12", actual: null,    forecast: 1220000, upper: 1290000, lower: 1150000 },
  { week: "W13", actual: null,    forecast: 1265000, upper: 1360000, lower: 1170000 },
  { week: "W14", actual: null,    forecast: 1310000, upper: 1430000, lower: 1190000 },
  { week: "W15", actual: null,    forecast: 1348000, upper: 1490000, lower: 1206000 },
];

export const REALLOCATION_SUGGESTIONS = [
  { from: "NYC Midtown Tower",    to: "Alpine Lodge Zermatt", amount: 8000,  reason: "Saturation → high headroom" },
  { from: "Harbour View Dubai",   to: "Costa del Sol Resort", amount: 5000,  reason: "Saturation → high headroom" },
  { from: "NYC Midtown Tower",    to: "Park Lane Residences", amount: 4000,  reason: "Low marginal ROAS → high mROAS" },
];
