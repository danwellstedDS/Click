import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, Button, Form, Input } from "@derbysoft/neat-design";
import { useAuth } from "./AuthContext";
import { ApiError } from "../../lib/apiClient";

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
    <div style={{ maxWidth: 400, margin: "80px auto", padding: 24 }}>
      <Form onFinish={handleSubmit} layout="vertical">
        <Form.Item
          name="email"
          label="Email"
          rules={[{ required: true, type: "email" }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          name="password"
          label="Password"
          rules={[{ required: true }]}
        >
          <Input.Password />
        </Form.Item>
        {error && (
          <Alert
            type="error"
            message={error}
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}
        <Form.Item>
          <Button variant="primary" htmlType="submit" loading={isSubmitting} block>
            Sign in
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
}
