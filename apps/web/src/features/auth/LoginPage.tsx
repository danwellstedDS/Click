import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, Button, Form, Input } from "@derbysoft/neat-design";
import { useAuth } from "./AuthContext";
import { ApiError } from "../../lib/apiClient";
import logo from "../../assets/logo.svg";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(values: { email: string; password: string }) {
    setError(null);
    setIsSubmitting(true);
    try {
      const tenants = await login(values.email, values.password);
      navigate(tenants.length > 1 ? "/select-tenant" : "/");
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "An unexpected error occurred. Please try again."
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div style={{ minHeight: "100vh", position: "relative", overflow: "hidden", background: "#fff" }}>
      {/* Blob 1 — lower-right, teal */}
      <div style={{
        position: "absolute", bottom: -120, right: -80,
        width: 420, height: 420,
        background: "radial-gradient(circle, rgba(0,224,225,0.35) 0%, rgba(0,224,225,0) 70%)",
        borderRadius: "50%",
        pointerEvents: "none",
      }} />
      {/* Blob 2 — lower-right offset, indigo accent */}
      <div style={{
        position: "absolute", bottom: -60, right: 60,
        width: 280, height: 280,
        background: "radial-gradient(circle, rgba(99,102,241,0.2) 0%, rgba(99,102,241,0) 70%)",
        borderRadius: "50%",
        pointerEvents: "none",
      }} />

      <div style={{ maxWidth: 400, margin: "0 auto", padding: "80px 24px 24px", position: "relative", zIndex: 1 }}>
        <img src={logo} alt="DerbySoft" style={{ height: 32, marginBottom: 24 }} />
        <h1 style={{ fontSize: 28, fontWeight: 600, color: "#00131C", marginBottom: 24 }}>Sign In</h1>

        <Form onFinish={handleSubmit} layout="vertical">
          <Form.Item name="email" label="Work Email" rules={[{ required: true, type: "email" }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true }]}>
            <Input.Password />
          </Form.Item>
          <div style={{ textAlign: "right", marginBottom: 16 }}>
            <a href="#" onClick={e => e.preventDefault()} style={{ color: "#1677ff", fontSize: 14 }}>
              Forgot Password?
            </a>
          </div>
          {error && (
            <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />
          )}
          <Form.Item>
            <Button variant="primary" htmlType="submit" loading={isSubmitting} block>
              Sign in
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}
