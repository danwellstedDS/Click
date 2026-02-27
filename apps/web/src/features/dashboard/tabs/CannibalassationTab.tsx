import { Card, Col, Row, Statistic, Table, Tag } from "@derbysoft/neat-design";
import { SwapOutlined, RiseOutlined } from "@ant-design/icons";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import { OVERLAP_MATRIX, OVERLAP_PROPERTY_NAMES, CPC_INFLATION } from "../mockData";

// Aggregate stats
const allScores = OVERLAP_MATRIX.flatMap((row) => Object.values(row.scores).filter((v) => v > 0));
const overlapPairs = allScores.filter((v) => v >= 50).length / 2;
const avgImpressionConflict = (allScores.reduce((s, v) => s + v, 0) / allScores.length).toFixed(0);
const latestInflation = CPC_INFLATION[CPC_INFLATION.length - 1].inflation.toFixed(2);

function overlapColor(score: number): string {
  if (score === 0) return "default";
  if (score >= 70) return "red";
  if (score >= 40) return "orange";
  return "green";
}

// Overlap matrix table columns
const matrixColumns = [
  { title: "", dataIndex: "property", key: "property", fixed: "left" as const, width: 90, render: (v: string) => <strong style={{ fontSize: 11 }}>{v}</strong> },
  ...OVERLAP_PROPERTY_NAMES.map((name) => ({
    title: <span style={{ fontSize: 11 }}>{name}</span>,
    key: name,
    width: 80,
    render: (_: unknown, record: { property: string; scores: Record<string, number> }) => {
      if (record.property === name) return <span style={{ color: "#bdc3c7" }}>—</span>;
      const score = record.scores[name] ?? 0;
      return <Tag color={overlapColor(score)} style={{ fontSize: 10, padding: "0 4px" }}>{score}</Tag>;
    },
  })),
];

const matrixData = OVERLAP_MATRIX.map((row, i) => ({ key: i, property: row.property, scores: row.scores }));

// Brand term cross-bidding table
const BRAND_CONFLICTS = [
  { key: 1, property: "NYC Midtown Tower",    term: "Grand Mayfair London",   cpc: "£3.20", severity: "High" },
  { key: 2, property: "Harbour View Dubai",   term: "Marina Bay Suites",      cpc: "£2.10", severity: "Medium" },
  { key: 3, property: "Costa del Sol Resort", term: "Alpine Lodge Zermatt",   cpc: "£1.40", severity: "Low" },
  { key: 4, property: "Sydney Harbourside",   term: "Marina Bay Suites",      cpc: "£1.80", severity: "Medium" },
];

const brandColumns = [
  { title: "Bidding Property",  dataIndex: "property",  key: "property" },
  { title: "Brand Term",        dataIndex: "term",       key: "term" },
  { title: "Avg CPC",           dataIndex: "cpc",        key: "cpc" },
  {
    title: "Severity",
    dataIndex: "severity",
    key: "severity",
    render: (v: string) => {
      const color = v === "High" ? "red" : v === "Medium" ? "orange" : "green";
      return <Tag color={color}>{v}</Tag>;
    },
  },
];

export function CannibalassationTab() {
  return (
    <div>
      {/* Stat row */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Overlapping Auction Pairs"
              value={Math.round(overlapPairs)}
              prefix={<SwapOutlined style={{ color: "#f39c12" }} />}
              valueStyle={{ color: "#f39c12" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Internal CPC Inflation Index"
              value={latestInflation}
              suffix="x"
              prefix={<RiseOutlined style={{ color: "#e74c3c" }} />}
              valueStyle={{ color: Number(latestInflation) > 1.1 ? "#e74c3c" : "#2ecc71" }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Avg Impression Share Conflict"
              value={avgImpressionConflict}
              suffix="%"
              valueStyle={{ color: Number(avgImpressionConflict) > 40 ? "#f39c12" : "#2ecc71" }}
            />
          </Card>
        </Col>
      </Row>

      {/* Row 2: Overlap matrix */}
      <Row style={{ marginBottom: 24 }}>
        <Col xs={24}>
          <Card title="Auction Overlap Matrix — Conflict Score (0–100, red = severe)">
            <div style={{ overflowX: "auto" }}>
              <Table
                dataSource={matrixData}
                columns={matrixColumns}
                rowKey="key"
                pagination={false}
                size="small"
                scroll={{ x: true }}
              />
            </div>
          </Card>
        </Col>
      </Row>

      {/* Row 3: CPC inflation trend + brand term table */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={10}>
          <Card title="Internal CPC Inflation Trend">
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={CPC_INFLATION} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis domain={[1.0, 1.3]} tickFormatter={(v) => `${v.toFixed(2)}x`} />
                <Tooltip formatter={(v: number) => [`${v.toFixed(2)}x`, "Inflation"]} />
                <Line type="monotone" dataKey="inflation" stroke="#e74c3c" strokeWidth={2} dot={{ fill: "#e74c3c" }} name="CPC Inflation" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={14}>
          <Card title="Brand Term Cross-Bidding Conflicts">
            <Table
              dataSource={BRAND_CONFLICTS}
              columns={brandColumns}
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
