import { Card, Col, Row, Statistic } from "@derbysoft/neat-design";
import {
  RiseOutlined,
  FallOutlined,
  DollarOutlined,
  PercentageOutlined,
} from "@ant-design/icons";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
  LineChart,
  Line,
} from "recharts";
import {
  PROPERTIES,
  REVENUE_TREND,
  REVPAR_UPLIFT,
  CHANNEL_MIX,
} from "../mockData";

const COLORS = ["#3498db", "#e74c3c", "#2ecc71", "#f39c12"];

const totalRevenue = PROPERTIES.reduce((s, p) => s + p.revenue, 0);
const totalSpend   = PROPERTIES.reduce((s, p) => s + p.spend, 0);
const portfolioRoas = totalRevenue / totalSpend;
const contributionMargin = ((totalRevenue - totalSpend) / totalRevenue) * 100;

export function PortfolioOverviewTab() {
  return (
    <div>
      {/* Stat row */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Revenue"
              value={totalRevenue}
              prefix={<DollarOutlined />}
              suffix="£"
              valueStyle={{ color: "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Spend"
              value={totalSpend}
              prefix={<DollarOutlined />}
              suffix="£"
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Portfolio ROAS"
              value={portfolioRoas.toFixed(2)}
              prefix={<RiseOutlined style={{ color: "#2ecc71" }} />}
              suffix="x"
              valueStyle={{ color: "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Contribution Margin"
              value={contributionMargin.toFixed(1)}
              prefix={<PercentageOutlined />}
              suffix="%"
              valueStyle={{ color: contributionMargin > 70 ? "#2ecc71" : "#f39c12" }}
            />
          </Card>
        </Col>
      </Row>

      {/* Row 2: RevPAR uplift + channel mix */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} lg={14}>
          <Card title="RevPAR Uplift vs Baseline (%)">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={REVPAR_UPLIFT} margin={{ top: 4, right: 16, left: 0, bottom: 48 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" angle={-35} textAnchor="end" interval={0} tick={{ fontSize: 11 }} />
                <YAxis tickFormatter={(v) => `${v}%`} />
                <Tooltip formatter={(v) => [`${v}%`, "Uplift"]} />
                <Bar dataKey="uplift" fill="#3498db" radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="Direct vs OTA Channel Mix">
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie
                  data={CHANNEL_MIX}
                  cx="50%"
                  cy="45%"
                  outerRadius={90}
                  dataKey="value"
                  label={({ name, value }) => `${name} ${value}%`}
                  labelLine={false}
                >
                  {CHANNEL_MIX.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip formatter={(v) => [`${v}%`]} />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* Row 3: Portfolio revenue trend */}
      <Row>
        <Col xs={24}>
          <Card title="Portfolio Revenue Trend — Last 12 Weeks">
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={REVENUE_TREND} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis tickFormatter={(v) => `£${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={(v: number) => [`£${(v / 1000).toFixed(0)}k`]} />
                <Legend />
                <Line type="monotone" dataKey="revenue"  stroke="#3498db" strokeWidth={2} dot={false} name="Revenue" />
                <Line type="monotone" dataKey="baseline" stroke="#bdc3c7" strokeWidth={1} strokeDasharray="4 4" dot={false} name="Baseline" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
