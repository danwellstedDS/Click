import { Card, Col, Row, Statistic, Table, Tag } from "@derbysoft/neat-design";
import { RiseOutlined, FallOutlined, ClockCircleOutlined, CheckCircleOutlined } from "@ant-design/icons";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
  BarChart,
  Bar,
  ReferenceLine,
} from "recharts";
import { PACE_DATA, FORECAST_BAND, REALLOCATION_SUGGESTIONS } from "../mockData";

const totalRevenue  = PACE_DATA.reduce((s, p) => s + p.actual, 0);
const totalTarget   = PACE_DATA.reduce((s, p) => s + p.target, 0);
const revPacing     = ((totalRevenue / totalTarget) * 100).toFixed(1);

// Mock spend pacing
const spendPacing   = "93.2";
const offPaceCount  = PACE_DATA.filter((p) => p.status !== "on-track").length;
const forecastConf  = "±8%";

function statusTag(status: string) {
  if (status === "on-track") return <Tag color="green">On Track</Tag>;
  if (status === "at-risk")  return <Tag color="orange">At Risk</Tag>;
  return                            <Tag color="red">Behind</Tag>;
}

const paceColumns = [
  { title: "Property",    dataIndex: "name",    key: "name" },
  { title: "Target (£)",  dataIndex: "target",  key: "target",  render: (v: number) => `£${(v / 1000).toFixed(0)}k` },
  { title: "Actual (£)",  dataIndex: "actual",  key: "actual",  render: (v: number) => `£${(v / 1000).toFixed(0)}k` },
  { title: "Pacing %",    dataIndex: "pacing",  key: "pacing",  render: (v: number) => `${v}%` },
  { title: "Status",      dataIndex: "status",  key: "status",  render: (v: string) => statusTag(v) },
];

const reallocColumns = [
  { title: "From",   dataIndex: "from",   key: "from" },
  { title: "To",     dataIndex: "to",     key: "to" },
  { title: "Amount", dataIndex: "amount", key: "amount", render: (v: number) => `£${(v / 1000).toFixed(0)}k` },
  { title: "Reason", dataIndex: "reason", key: "reason", render: (v: string) => <span style={{ fontSize: 12 }}>{v}</span> },
];

const reallocationData = REALLOCATION_SUGGESTIONS.map((r, i) => ({ ...r, key: i }));

export function PaceForecastTab() {
  return (
    <div>
      {/* Stat row */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Revenue Pacing"
              value={revPacing}
              suffix="%"
              prefix={Number(revPacing) >= 95 ? <CheckCircleOutlined style={{ color: "#2ecc71" }} /> : <ClockCircleOutlined style={{ color: "#f39c12" }} />}
              valueStyle={{ color: Number(revPacing) >= 95 ? "#2ecc71" : "#f39c12" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Spend Pacing"
              value={spendPacing}
              suffix="%"
              prefix={<RiseOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Off-Pace Hotels"
              value={offPaceCount}
              prefix={<FallOutlined style={{ color: offPaceCount > 0 ? "#e74c3c" : "#2ecc71" }} />}
              valueStyle={{ color: offPaceCount > 0 ? "#e74c3c" : "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Forecast Confidence"
              value={forecastConf}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Row 2: Forecast area chart + pacing bar */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} lg={14}>
          <Card title="Revenue Forecast with Confidence Band (£)">
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart data={FORECAST_BAND} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis tickFormatter={(v) => `£${(v / 1000).toFixed(0)}k`} />
                <Tooltip
                  formatter={(v: number, name: string) => {
                    const labels: Record<string, string> = { actual: "Actual", forecast: "Forecast", upper: "Upper band", lower: "Lower band" };
                    return [`£${(v / 1000).toFixed(0)}k`, labels[name] ?? name];
                  }}
                />
                <Legend />
                <Area type="monotone" dataKey="upper"    stroke="none"    fill="#3498db" fillOpacity={0.1} name="upper" />
                <Area type="monotone" dataKey="lower"    stroke="none"    fill="#fff"    fillOpacity={1}   name="lower" />
                <Area type="monotone" dataKey="forecast" stroke="#3498db" fill="#3498db" fillOpacity={0.2} strokeWidth={2} name="forecast" strokeDasharray="5 5" />
                <Area type="monotone" dataKey="actual"   stroke="#2ecc71" fill="none"   strokeWidth={2}  name="actual" />
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="Pacing vs Budget — Per Property">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={PACE_DATA} margin={{ top: 4, right: 16, left: 0, bottom: 48 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" angle={-35} textAnchor="end" interval={0} tick={{ fontSize: 11 }} />
                <YAxis tickFormatter={(v) => `${v}%`} domain={[0, 120]} />
                <Tooltip formatter={(v) => [`${v}%`, "Pacing"]} />
                <ReferenceLine y={100} stroke="#2ecc71" strokeDasharray="4 4" label={{ value: "Target", position: "insideTopRight", fontSize: 10 }} />
                <Bar dataKey="pacing" radius={[3, 3, 0, 0]}
                  fill="#3498db"
                  label={false}
                />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* Row 3: Off-pace alert table + reallocation */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card title="Off-Pace Hotel Alerts">
            <Table
              dataSource={PACE_DATA.map((p, i) => ({ ...p, key: i }))}
              columns={paceColumns}
              rowKey="key"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="Suggested Reallocation">
            <Table
              dataSource={reallocationData}
              columns={reallocColumns}
              rowKey="key"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
