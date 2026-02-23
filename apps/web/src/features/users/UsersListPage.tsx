import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Button,
  Form,
  Input,
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
import type { CreateUserRequest, UserListItem } from "./types";

export function UsersListPage() {
  const navigate = useNavigate();
  const [users, setUsers] = useState<UserListItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [addForm] = Form.useForm();

  const [tempPasswordModal, setTempPasswordModal] = useState<{
    open: boolean;
    email: string;
    password: string;
  }>({ open: false, email: "", password: "" });

  const [deleteModal, setDeleteModal] = useState<{
    open: boolean;
    user: UserListItem | null;
  }>({ open: false, user: null });
  const [deleteLoading, setDeleteLoading] = useState(false);

  async function loadUsers() {
    setLoading(true);
    try {
      const data = await usersApi.listUsers();
      setUsers(data);
    } catch {
      toast.error("Failed to load users");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  async function handleAddUser(values: CreateUserRequest) {
    setAddLoading(true);
    try {
      const result = await usersApi.createUser(values);
      setAddModalOpen(false);
      addForm.resetFields();
      setTempPasswordModal({
        open: true,
        email: result.user.email,
        password: result.temporaryPassword,
      });
      await loadUsers();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Failed to create user";
      toast.error(message);
    } finally {
      setAddLoading(false);
    }
  }

  async function handleDeleteConfirm() {
    if (!deleteModal.user) return;
    setDeleteLoading(true);
    try {
      await usersApi.deleteUser(deleteModal.user.id);
      setDeleteModal({ open: false, user: null });
      setUsers((prev) => prev.filter((u) => u.id !== deleteModal.user!.id));
      toast.success("User deleted");
    } catch {
      toast.error("Failed to delete user");
    } finally {
      setDeleteLoading(false);
    }
  }

  const columns = [
    {
      title: "Email",
      dataIndex: "email",
      key: "email",
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
      dataIndex: "createdAt",
      key: "createdAt",
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
    {
      title: "Actions",
      key: "actions",
      render: (_: unknown, record: UserListItem) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button
            variant="link"
            onClick={() => navigate(`/users/${record.id}`)}
          >
            View
          </Button>
          <Button
            variant="link"
            onClick={() => setDeleteModal({ open: true, user: record })}
          >
            Delete
          </Button>
        </div>
      ),
    },
  ];

  return (
    <AppLayout title="Users" breadcrumb="Users">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <Typography.Title level={5} style={{ margin: 0 }}>
          Manage tenant users
        </Typography.Title>
        <Button variant="primary" onClick={() => setAddModalOpen(true)}>
          Add User
        </Button>
      </div>

      {loading ? (
        <Spinner />
      ) : (
        <Table dataSource={users} columns={columns} rowKey="id" />
      )}

      {/* Add User Modal */}
      <Modal
        title="Add User"
        open={addModalOpen}
        onCancel={() => {
          setAddModalOpen(false);
          addForm.resetFields();
        }}
        footer={null}
      >
        <Form form={addForm} layout="vertical" onFinish={handleAddUser}>
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: "Email is required" },
              { type: "email", message: "Enter a valid email address" },
            ]}
          >
            <Input placeholder="user@example.com" />
          </Form.Item>
          <Form.Item
            name="role"
            label="Role"
            rules={[{ required: true, message: "Role is required" }]}
          >
            <Select
              placeholder="Select role"
              options={[
                { value: "ADMIN", label: "Admin" },
                { value: "VIEWER", label: "Viewer" },
              ]}
            />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0, textAlign: "right" }}>
            <Button
              variant="secondary"
              onClick={() => {
                setAddModalOpen(false);
                addForm.resetFields();
              }}
              style={{ marginRight: 8 }}
            >
              Cancel
            </Button>
            <Button variant="primary" type="submit" loading={addLoading}>
              {addLoading ? "Creating…" : "Create User"}
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Temporary Password Modal */}
      <Modal
        title="User Created"
        open={tempPasswordModal.open}
        onOk={() => setTempPasswordModal({ open: false, email: "", password: "" })}
        onCancel={() => setTempPasswordModal({ open: false, email: "", password: "" })}
        cancelButtonProps={{ style: { display: "none" } }}
      >
        <p>
          User <strong>{tempPasswordModal.email}</strong> has been created. Share
          the following temporary password — it will not be shown again:
        </p>
        <div
          style={{
            background: "#f5f5f5",
            padding: "12px 16px",
            borderRadius: 6,
            fontFamily: "monospace",
            fontSize: 14,
            wordBreak: "break-all",
            userSelect: "all",
          }}
        >
          {tempPasswordModal.password}
        </div>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        title="Delete User"
        open={deleteModal.open}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModal({ open: false, user: null })}
        okText="Delete"
        okButtonProps={{ loading: deleteLoading }}
      >
        <p>
          Are you sure you want to delete{" "}
          <strong>{deleteModal.user?.email}</strong>? This action cannot be undone.
        </p>
      </Modal>
    </AppLayout>
  );
}
