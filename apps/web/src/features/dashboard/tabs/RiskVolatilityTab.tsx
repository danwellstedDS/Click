import { Card, Col, Row, Statistic, Table, Tag } from "@derbysoft/neat-design";
import { AlertOutlined, RiseOutlined } from "@ant-design/icons";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
  Legend,
} from "recharts";
import { PROPERTIES, CVR_VOLATILITY, SOV_TREND } from "../mockData";

const totalRevenue = PROPERTIES.reduce((s, p) => s + p.revenue, 0);
const topFive      = [...PROPERTIES].sort((a, b) => b.revenue - a.revenue).slice(0, 5);

// Herfindahl index (0–1) as revenue concentration
const revenueConcentration = (
  PROPERTIES.reduce((s, p) => s + (p.revenue / totalRevenue) ** 2, 0) * 100
).toFixed(1);

const totalSpend = PROPERTIES.reduce((s, p) => s + p.spend, 0);
const spendConcentration = (
  PROPERTIES.reduce((s, p) => s + (p.spend / totalSpend) ** 2, 0) * 100
).toFixed(1);

const avgOta = (PROPERTIES.reduce((s, p) => s + p.otaDependency, 0) / PROPERTIES.length).toFixed(1);

// Synthetic CVR volatility score
const cvrScore = 3.2;

const top5Bar = topFive.map((p) => ({
  name: p.name.split(" ").slice(0, 2).join(" "),
  share: parseFloat(((p.revenue / totalRevenue) * 100).toFixed(1)),
}));

function riskTag(otaDep: number) {
  if (otaDep >= 55) return <Tag color="red">High risk</Tag>;
  if (otaDep >= 40) return <Tag color="orange">Medium</Tag>;
  return <Tag color="green">Low risk</Tag>;
}

const marketTable = PROPERTIES.map((p) => ({
  key: p.id,
  name: p.name,
  otaDependency: p.otaDependency,
}));

const columns = [
  { title: "Property", dataIndex: "name", key: "name" },
  { title: "OTA Dependency", dataIndex: "otaDependency", key: "otaDependency", render: (v: number) => `${v}%` },
  { title: "Risk", key: "risk", render: (_: unknown, r: typeof marketTable[0]) => riskTag(r.otaDependency) },
];

export function RiskVolatilityTab() {
  return (
    <div>
      {/* Stat row */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Revenue Concentration Index"
              value={revenueConcentration}
              suffix="%"
              prefix={<AlertOutlined style={{ color: Number(revenueConcentration) > 25 ? "#e74c3c" : "#2ecc71" }} />}
              valueStyle={{ color: Number(revenueConcentration) > 25 ? "#e74c3c" : "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Spend Concentration Index"
              value={spendConcentration}
              suffix="%"
              prefix={<AlertOutlined style={{ color: Number(spendConcentration) > 25 ? "#f39c12" : "#2ecc71" }} />}
              valueStyle={{ color: Number(spendConcentration) > 25 ? "#f39c12" : "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="CVR Volatility Score"
              value={cvrScore.toFixed(1)}
              suffix="σ"
              prefix={<RiseOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="OTA Dependency"
              value={avgOta}
              suffix="%"
              valueStyle={{ color: Number(avgOta) > 50 ? "#e74c3c" : "#f39c12" }}
            />
          </Card>
        </Col>
      </Row>

      {/* Row 2: Top 5 revenue concentration + CVR volatility */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="Revenue Concentration — Top 5 Properties (% of portfolio)">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={top5Bar} margin={{ top: 4, right: 16, left: 0, bottom: 48 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" angle={-35} textAnchor="end" interval={0} tick={{ fontSize: 11 }} />
                <YAxis tickFormatter={(v) => `${v}%`} />
                <Tooltip formatter={(v) => [`${v}%`, "Revenue share"]} />
                <Bar dataKey="share" fill="#e74c3c" radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="CVR Volatility — Last 4 Weeks (%)">
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={CVR_VOLATILITY} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis tickFormatter={(v) => `${v}%`} />
                <Tooltip formatter={(v: number) => [`${v}%`]} />
                <Legend />
                <Line type="monotone" dataKey="mayfair"  stroke="#3498db" strokeWidth={2} name="Mayfair" />
                <Line type="monotone" dataKey="dubai"    stroke="#e74c3c" strokeWidth={2} name="Dubai" />
                <Line type="monotone" dataKey="parkLane" stroke="#2ecc71" strokeWidth={2} name="Park Lane" />
                <Line type="monotone" dataKey="marina"   stroke="#f39c12" strokeWidth={2} name="Marina Bay" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* Row 3: Market dependency table + SOV trend */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Market Dependency Risk">
            <Table
              dataSource={marketTable}
              columns={columns}
              rowKey="key"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Competitor Share-of-Voice Trend (%)">
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={SOV_TREND} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis tickFormatter={(v) => `${v}%`} />
                <Tooltip formatter={(v: number) => [`${v}%`]} />
                <Legend />
                <Line type="monotone" dataKey="portfolio"   stroke="#3498db" strokeWidth={2} name="Our Portfolio" />
                <Line type="monotone" dataKey="competitor1" stroke="#e74c3c" strokeWidth={2} name="Competitor A" />
                <Line type="monotone" dataKey="competitor2" stroke="#f39c12" strokeWidth={2} name="Competitor B" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
