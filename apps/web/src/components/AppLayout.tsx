import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Layout,
  Menu,
  Avatar,
  Badge,
  Breadcrumb,
  Typography,
  Space,
  Dropdown,
  Button,
  Divider,
} from "@derbysoft/neat-design";
import {
  HomeOutlined,
  BellOutlined,
  QuestionCircleOutlined,
  DownOutlined,
  LeftOutlined,
  RightOutlined,
  AppstoreOutlined,
  BankOutlined,
  TeamOutlined,
  UserOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import { useAuth } from "../features/auth/AuthContext";
import logo from "../assets/logo.svg";

const NAV_ROUTES: Record<string, string> = {
  users: "/users",
  properties: "/properties",
};

const NAV_ITEMS = [
  { key: "properties", icon: <HomeOutlined />, label: "Properties" },
  { key: "users", icon: <TeamOutlined />, label: "Users" },
];

const HEADER_HEIGHT = 56;

interface AppLayoutProps {
  title: string;
  breadcrumb?: string;
  children: React.ReactNode;
}

export function AppLayout({ title, breadcrumb, children }: AppLayoutProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(true);
  const initial = user?.email?.[0]?.toUpperCase() ?? "?";

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  const profileMenuItems = [
    { key: "profile", icon: <UserOutlined />, label: "Profile" },
    { type: "divider" },
    { key: "logout", icon: <LogoutOutlined />, label: "Sign out" },
  ];

  return (
    <Layout style={{ minHeight: "100vh" }}>

      {/* ── Top bar ──────────────────────────────────────── */}
      <Layout.Header
        style={{
          background: "#fff",
          borderBottom: "1px solid rgba(0,0,0,0.08)",
          height: HEADER_HEIGHT,
          lineHeight: `${HEADER_HEIGHT}px`,
          padding: "0 16px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          position: "sticky",
          top: 0,
          zIndex: 200,
        }}
      >
        {/* Left: grid icon + logo + account selector */}
        <Space size={0} align="center">
          <AppstoreOutlined
            style={{ fontSize: 20, color: "rgba(0,0,0,0.55)", cursor: "pointer", padding: "0 14px" }}
          />
          <Divider type="vertical" style={{ height: 24, margin: "0 8px" }} />
          <img src={logo} alt="DerbySoft" style={{ height: 28, display: "block" }} />
          <Divider type="vertical" style={{ height: 24, margin: "0 12px" }} />
          <Dropdown menu={{ items: [] }} trigger={["click"]}>
            <Button
              style={{
                display: "flex",
                alignItems: "center",
                gap: 6,
                paddingInline: 10,
                fontWeight: 500,
              }}
            >
              <BankOutlined style={{ color: "rgba(0,0,0,0.55)" }} />
              {user?.tenantName ?? "Account Name"}
              <DownOutlined style={{ fontSize: 10, color: "rgba(0,0,0,0.45)", marginLeft: 2 }} />
            </Button>
          </Dropdown>
        </Space>

        {/* Right: bell, help, avatar */}
        <Space size={16}>
          <Badge count={99} overflowCount={99} size="small">
            <BellOutlined
              style={{ fontSize: 18, cursor: "pointer", color: "rgba(0,0,0,0.65)" }}
            />
          </Badge>
          <QuestionCircleOutlined
            style={{ fontSize: 18, cursor: "pointer", color: "rgba(0,0,0,0.65)" }}
          />
          <Dropdown
            menu={{
              items: profileMenuItems,
              onClick: ({ key }) => {
                if (key === "profile") navigate("/profile");
                if (key === "logout") handleLogout();
              },
            }}
            trigger={["click"]}
          >
            <Avatar size="small" style={{ background: "#e74c3c", cursor: "pointer" }}>
              {initial}
            </Avatar>
          </Dropdown>
        </Space>
      </Layout.Header>

      {/* ── Body: collapsible sidebar + main content ──── */}
      <Layout style={{ flex: 1 }}>

        {/* Sidebar — custom trigger so we own the z-index */}
        <Layout.Sider
          trigger={null}
          collapsed={collapsed}
          theme="light"
          width={220}
          collapsedWidth={56}
          style={{
            borderRight: "1px solid rgba(0,0,0,0.08)",
            height: `calc(100vh - ${HEADER_HEIGHT}px)`,
            position: "sticky",
            top: HEADER_HEIGHT,
            overflow: "hidden",
            display: "flex",
            flexDirection: "column",
            zIndex: 10,
          }}
        >
          {/* Nav menu — scrollable if needed */}
          <div style={{ flex: 1, overflowY: "auto" }}>
            <Menu
              mode="inline"
              items={NAV_ITEMS}
              style={{ borderRight: 0 }}
              onClick={({ key }) => {
                if (NAV_ROUTES[key]) navigate(NAV_ROUTES[key]);
              }}
            />
          </div>

          {/* Custom collapse trigger */}
          <div
            onClick={() => setCollapsed(!collapsed)}
            style={{
              height: 48,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              cursor: "pointer",
              borderTop: "1px solid rgba(0,0,0,0.08)",
              color: "rgba(0,0,0,0.45)",
              flexShrink: 0,
              transition: "color 0.2s",
            }}
          >
            {collapsed ? <RightOutlined /> : <LeftOutlined />}
          </div>
        </Layout.Sider>

        {/* Right column: page header + scrollable content */}
        <Layout>
          {/* Page header */}
          <div
            style={{
              background: "#fff",
              padding: "16px 24px",
              borderBottom: "1px solid rgba(0,0,0,0.08)",
            }}
          >
            <Typography.Title level={4} style={{ margin: 0 }}>
              {title}
            </Typography.Title>
            {breadcrumb && (
              <Breadcrumb
                items={[{ title: breadcrumb }]}
                style={{ marginTop: 4 }}
              />
            )}
          </div>

          {/* Content */}
          <Layout.Content style={{ padding: 24, overflowY: "auto" }}>
            {children}
          </Layout.Content>
        </Layout>

      </Layout>
    </Layout>
  );
}
