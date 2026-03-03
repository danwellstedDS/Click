import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Breadcrumb,
  Button,
  Form,
  Modal,
  Select,
  Spinner,
  Table,
  Tag,
  toast,
  Typography,
} from "@derbysoft/neat-design";
import { AppLayout } from "../../components/AppLayout";
import * as usersApi from "./usersApi";
import type { UserDetail, UserRole } from "./types";

const ROLE_OPTIONS = [
  { value: "VIEWER", label: "Viewer" },
  { value: "ANALYST", label: "Analyst" },
  { value: "MANAGER", label: "Manager" },
  { value: "ADMIN", label: "Admin" },
  { value: "SUPPORT", label: "Support" },
];

export function UserDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [user, setUser] = useState<UserDetail | null>(null);
  const [loading, setLoading] = useState(true);

  const [deleteModal, setDeleteModal] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const [editModal, setEditModal] = useState(false);
  const [editLoading, setEditLoading] = useState(false);
  const [editForm] = Form.useForm();

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    usersApi
      .getUser(id)
      .then((data) => setUser(data))
      .catch(() => toast.error("Failed to load user"))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleEditRole(values: { role: UserRole }) {
    if (!id) return;
    setEditLoading(true);
    try {
      const updated = await usersApi.updateUserRole(id, { role: values.role });
      setUser((prev) => prev ? { ...prev, role: updated.role } : prev);
      setEditModal(false);
      editForm.resetFields();
      toast.success("Role updated");
    } catch {
      toast.error("Failed to update role");
    } finally {
      setEditLoading(false);
    }
  }

  async function handleDelete() {
    if (!id) return;
    setDeleteLoading(true);
    try {
      await usersApi.deleteUser(id);
      toast.success("User deleted");
      navigate("/users");
    } catch {
      toast.error("Failed to delete user");
    } finally {
      setDeleteLoading(false);
    }
  }

  const membershipColumns = [
    {
      title: "Tenant ID",
      dataIndex: "tenantId",
      key: "tenantId",
      render: (tenantId: string) => (
        <Typography.Text code style={{ fontSize: 12 }}>
          {tenantId}
        </Typography.Text>
      ),
    },
    {
      title: "Role",
      dataIndex: "role",
      key: "role",
      render: (role: string) => (
        <Tag color={role === "ADMIN" ? "blue" : "default"}>{role}</Tag>
      ),
    },
    {
      title: "Member Since",
      dataIndex: "memberSince",
      key: "memberSince",
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
  ];

  if (loading) {
    return (
      <AppLayout title="User Detail" breadcrumb="Users">
        <Spinner />
      </AppLayout>
    );
  }

  if (!user) {
    return (
      <AppLayout title="User Not Found" breadcrumb="Users">
        <Button variant="link" onClick={() => navigate("/users")}>
          ← Back to Users
        </Button>
        <p>User not found.</p>
      </AppLayout>
    );
  }

  return (
    <AppLayout title={user.email} breadcrumb="Users">
      <Breadcrumb
        items={[
          { title: "Users", href: "/users" },
          { title: user.email },
        ]}
        style={{ marginBottom: 16 }}
      />

      {/* User Info */}
      <div
        style={{
          background: "#fff",
          border: "1px solid rgba(0,0,0,0.08)",
          borderRadius: 8,
          padding: 24,
          marginBottom: 24,
        }}
      >
        <Typography.Title level={5} style={{ marginTop: 0, marginBottom: 16 }}>
          Account Information
        </Typography.Title>
        <div style={{ display: "grid", gridTemplateColumns: "160px 1fr", gap: "12px 0" }}>
          <Typography.Text type="secondary">Email</Typography.Text>
          <Typography.Text>{user.email}</Typography.Text>

          <Typography.Text type="secondary">Role</Typography.Text>
          <span>
            <Tag color={user.role === "ADMIN" ? "blue" : "default"}>{user.role}</Tag>
          </span>

          <Typography.Text type="secondary">Account ID</Typography.Text>
          <Typography.Text code style={{ fontSize: 12 }}>
            {user.id}
          </Typography.Text>

          <Typography.Text type="secondary">Created</Typography.Text>
          <Typography.Text>{new Date(user.createdAt).toLocaleString()}</Typography.Text>

          <Typography.Text type="secondary">Last Updated</Typography.Text>
          <Typography.Text>{new Date(user.updatedAt).toLocaleString()}</Typography.Text>
        </div>
      </div>

      {/* Activity / Memberships */}
      <div
        style={{
          background: "#fff",
          border: "1px solid rgba(0,0,0,0.08)",
          borderRadius: 8,
          padding: 24,
          marginBottom: 24,
        }}
      >
        <Typography.Title level={5} style={{ marginTop: 0, marginBottom: 16 }}>
          Tenant Memberships
        </Typography.Title>
        <Table
          dataSource={user.memberships}
          columns={membershipColumns}
          rowKey="tenantId"
          pagination={false}
        />
      </div>

      {/* Actions */}
      <div style={{ display: "flex", gap: 12 }}>
        <Button variant="secondary" onClick={() => navigate("/users")}>
          ← Back to Users
        </Button>
        <Button
          variant="secondary"
          onClick={() => {
            editForm.setFieldsValue({ role: user.role });
            setEditModal(true);
          }}
        >
          Edit Role
        </Button>
        <Button variant="secondary" onClick={() => setDeleteModal(true)}>
          Delete User
        </Button>
      </div>

      {/* Edit Role Modal */}
      <Modal
        title="Edit Role"
        open={editModal}
        onCancel={() => {
          setEditModal(false);
          editForm.resetFields();
        }}
        footer={null}
      >
        <Form form={editForm} layout="vertical" onFinish={handleEditRole}>
          <Form.Item
            name="role"
            label="Role"
            rules={[{ required: true, message: "Role is required" }]}
          >
            <Select placeholder="Select role" options={ROLE_OPTIONS} />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0, textAlign: "right" }}>
            <Button
              variant="secondary"
              onClick={() => {
                setEditModal(false);
                editForm.resetFields();
              }}
              style={{ marginRight: 8 }}
            >
              Cancel
            </Button>
            <Button variant="primary" htmlType="submit" loading={editLoading}>
              {editLoading ? "Saving…" : "Save"}
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        title="Delete User"
        open={deleteModal}
        onOk={handleDelete}
        onCancel={() => setDeleteModal(false)}
        okText="Delete"
        okButtonProps={{ loading: deleteLoading }}
      >
        <p>
          Are you sure you want to delete <strong>{user.email}</strong>? This
          action cannot be undone.
        </p>
      </Modal>
    </AppLayout>
  );
}
