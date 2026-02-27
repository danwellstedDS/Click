import { Card, Col, Row, Statistic } from "@derbysoft/neat-design";
import { FundOutlined, WarningOutlined, BankOutlined } from "@ant-design/icons";
import {
  ScatterChart,
  Scatter,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
  ResponsiveContainer,
  BarChart,
  Bar,
  Legend,
  AreaChart,
  Area,
} from "recharts";
import { PROPERTIES, SPEND_TREND, SPEND_TREND_KEYS } from "../mockData";

const totalBudget    = PROPERTIES.reduce((s, p) => s + p.spend, 0);
const topProperty    = PROPERTIES.reduce((a, b) => (a.spend > b.spend ? a : b));
const topSpendShare  = ((topProperty.spend / totalBudget) * 100).toFixed(1);
const avgRoas        = PROPERTIES.reduce((s, p) => s + p.roas, 0) / PROPERTIES.length;
const underfunded    = PROPERTIES.filter((p) => p.roas > avgRoas && p.spend < totalBudget / PROPERTIES.length).length;

const scatterData = PROPERTIES.map((p) => ({
  name: p.name.split(" ").slice(0, 2).join(" "),
  spend: p.spend / 1000,
  revenue: p.revenue / 1000,
  roas: p.roas,
}));

const avgSpend   = totalBudget / PROPERTIES.length / 1000;
const totalRevenue = PROPERTIES.reduce((s, p) => s + p.revenue, 0);
const avgRevenue = totalRevenue / PROPERTIES.length / 1000;

const regionSpend = Object.entries(
  PROPERTIES.reduce<Record<string, number>>((acc, p) => {
    acc[p.region] = (acc[p.region] ?? 0) + p.spend / 1000;
    return acc;
  }, {})
).map(([region, spend]) => ({ region, spend: Math.round(spend) }));

const AREA_COLORS = [
  "#3498db","#e74c3c","#2ecc71","#f39c12","#9b59b6","#1abc9c","#e67e22","#34495e",
];

export function CapitalAllocationTab() {
  return (
    <div>
      {/* Stat row */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Total Budget Deployed"
              value={totalBudget}
              prefix={<FundOutlined />}
              suffix="£"
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Top Property Spend Share"
              value={topSpendShare}
              prefix={<BankOutlined />}
              suffix="%"
              valueStyle={{ color: Number(topSpendShare) > 30 ? "#f39c12" : "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Underfunded Properties"
              value={underfunded}
              prefix={<WarningOutlined style={{ color: underfunded > 0 ? "#e74c3c" : "#2ecc71" }} />}
              valueStyle={{ color: underfunded > 0 ? "#e74c3c" : "#2ecc71" }}
            />
          </Card>
        </Col>
      </Row>

      {/* Row 2: Scatter quadrant */}
      <Row style={{ marginBottom: 24 }}>
        <Col xs={24}>
          <Card title="Spend vs Revenue Quadrant (each dot = one property, £000s)">
            <ResponsiveContainer width="100%" height={320}>
              <ScatterChart margin={{ top: 16, right: 24, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="spend"   name="Spend £k"   type="number" label={{ value: "Spend (£k)", position: "insideBottom", offset: -4 }} />
                <YAxis dataKey="revenue" name="Revenue £k" type="number" label={{ value: "Revenue (£k)", angle: -90, position: "insideLeft" }} />
                <Tooltip
                  cursor={{ strokeDasharray: "3 3" }}
                  content={({ payload }) => {
                    if (!payload?.length) return null;
                    const d = payload[0].payload;
                    return (
                      <div style={{ background: "#fff", border: "1px solid #e8e8e8", borderRadius: 4, padding: "8px 12px", fontSize: 12 }}>
                        <strong>{d.name}</strong><br />
                        Spend: £{d.spend}k<br />
                        Revenue: £{d.revenue}k<br />
                        ROAS: {d.roas}x
                      </div>
                    );
                  }}
                />
                <ReferenceLine x={avgSpend}   stroke="#bdc3c7" strokeDasharray="4 4" label={{ value: "Avg spend",   position: "top",  fontSize: 10 }} />
                <ReferenceLine y={avgRevenue} stroke="#bdc3c7" strokeDasharray="4 4" label={{ value: "Avg revenue", position: "right", fontSize: 10 }} />
                <Scatter data={scatterData} fill="#3498db" />
              </ScatterChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* Row 3: Region bar + stacked area */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={10}>
          <Card title="Budget Distribution by Region (£k)">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={regionSpend} layout="vertical" margin={{ top: 4, right: 24, left: 60, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" tickFormatter={(v) => `£${v}k`} />
                <YAxis type="category" dataKey="region" tick={{ fontSize: 11 }} />
                <Tooltip formatter={(v) => [`£${v}k`, "Spend"]} />
                <Bar dataKey="spend" fill="#3498db" radius={[0, 3, 3, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={14}>
          <Card title="Allocation Trend — Last 12 Weeks (£)">
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart data={SPEND_TREND} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis tickFormatter={(v) => `£${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={(v: number) => [`£${(v / 1000).toFixed(0)}k`]} />
                <Legend wrapperStyle={{ fontSize: 10 }} />
                {SPEND_TREND_KEYS.map((key, i) => (
                  <Area
                    key={key}
                    type="monotone"
                    dataKey={key}
                    stackId="1"
                    stroke={AREA_COLORS[i % AREA_COLORS.length]}
                    fill={AREA_COLORS[i % AREA_COLORS.length]}
                    fillOpacity={0.7}
                  />
                ))}
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
