import { Card, Col, Row, Statistic, Table, Tag } from "@derbysoft/neat-design";
import { RiseOutlined, FallOutlined, AlertOutlined } from "@ant-design/icons";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  Cell,
  ReferenceLine,
} from "recharts";
import { PROPERTIES, MARGINAL_ROAS } from "../mockData";

const avgRoas        = PROPERTIES.reduce((s, p) => s + p.roas, 0) / PROPERTIES.length;
const avgMarginalRoas = PROPERTIES.reduce((s, p) => s + p.marginalRoas, 0) / PROPERTIES.length;
const headroomIndex  = PROPERTIES.reduce((s, p) => s + p.headroom, 0) / PROPERTIES.length;
const saturated      = PROPERTIES.filter((p) => p.headroom < 30).length;

function saturationStatus(headroom: number): { label: string; color: string } {
  if (headroom >= 60) return { label: "Healthy",   color: "green" };
  if (headroom >= 30) return { label: "Warning",   color: "orange" };
  return                     { label: "Saturated", color: "red" };
}

const saturationData = PROPERTIES.map((p) => ({
  key: p.id,
  name: p.name,
  roas: p.roas,
  marginalRoas: p.marginalRoas,
  headroom: p.headroom,
  ...saturationStatus(p.headroom),
}));

const headroomBarData = PROPERTIES.map((p) => ({
  name: p.name.split(" ").slice(0, 2).join(" "),
  headroom: p.headroom,
  fill: p.headroom >= 60 ? "#2ecc71" : p.headroom >= 30 ? "#f39c12" : "#e74c3c",
}));

const columns = [
  { title: "Property", dataIndex: "name", key: "name" },
  { title: "ROAS", dataIndex: "roas", key: "roas", render: (v: number) => `${v.toFixed(1)}x` },
  { title: "Marginal ROAS", dataIndex: "marginalRoas", key: "marginalRoas", render: (v: number) => `${v.toFixed(1)}x` },
  { title: "Headroom", dataIndex: "headroom", key: "headroom", render: (v: number) => `${v}%` },
  {
    title: "Status",
    dataIndex: "label",
    key: "label",
    render: (label: string, record: typeof saturationData[0]) => (
      <Tag color={record.color}>{label}</Tag>
    ),
  },
];

export function MarginalReturnTab() {
  return (
    <div>
      {/* Stat row */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Portfolio Avg ROAS"
              value={avgRoas.toFixed(2)}
              suffix="x"
              prefix={<RiseOutlined style={{ color: "#2ecc71" }} />}
              valueStyle={{ color: "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Portfolio Marginal ROAS"
              value={avgMarginalRoas.toFixed(2)}
              suffix="x"
              prefix={<RiseOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Headroom Index"
              value={headroomIndex.toFixed(0)}
              suffix="/100"
              prefix={headroomIndex >= 60 ? <RiseOutlined style={{ color: "#2ecc71" }} /> : <FallOutlined style={{ color: "#f39c12" }} />}
              valueStyle={{ color: headroomIndex >= 60 ? "#2ecc71" : "#f39c12" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Saturated Properties"
              value={saturated}
              prefix={<AlertOutlined style={{ color: saturated > 0 ? "#e74c3c" : "#2ecc71" }} />}
              valueStyle={{ color: saturated > 0 ? "#e74c3c" : "#2ecc71" }}
            />
          </Card>
        </Col>
      </Row>

      {/* Row 2: Diminishing returns curve */}
      <Row style={{ marginBottom: 24 }}>
        <Col xs={24}>
          <Card title="Spend vs Revenue Curve — Diminishing Returns (£000s)">
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart data={MARGINAL_ROAS} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="spend"   label={{ value: "Spend (£k)", position: "insideBottom", offset: -4 }} />
                <YAxis dataKey="revenue" label={{ value: "Revenue (£k)", angle: -90, position: "insideLeft" }} />
                <Tooltip formatter={(v: number, name: string) => [`${v}${name === "revenue" ? "k" : "x"}`, name === "revenue" ? "Revenue" : "Marginal ROAS"]} />
                <Area type="monotone" dataKey="revenue" stroke="#3498db" fill="#3498db" fillOpacity={0.2} name="revenue" />
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* Row 3: Headroom bar + saturation table */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Headroom Index per Property">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={headroomBarData} margin={{ top: 4, right: 16, left: 0, bottom: 48 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" angle={-35} textAnchor="end" interval={0} tick={{ fontSize: 11 }} />
                <YAxis domain={[0, 100]} tickFormatter={(v) => `${v}%`} />
                <Tooltip formatter={(v) => [`${v}%`, "Headroom"]} />
                <ReferenceLine y={30} stroke="#e74c3c" strokeDasharray="4 4" label={{ value: "Saturated threshold", position: "insideTopRight", fontSize: 10 }} />
                <ReferenceLine y={60} stroke="#f39c12" strokeDasharray="4 4" label={{ value: "Warning threshold", position: "insideTopRight", fontSize: 10 }} />
                <Bar dataKey="headroom" radius={[3, 3, 0, 0]}>
                  {headroomBarData.map((d, i) => (
                    <Cell key={i} fill={d.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Saturation Flags">
            <Table
              dataSource={saturationData}
              columns={columns}
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
