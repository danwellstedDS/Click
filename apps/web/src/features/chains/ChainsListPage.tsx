import { useEffect, useState } from "react";
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
import { useAuth } from "../auth/AuthContext";
import * as chainsApi from "./chainsApi";
import * as organizationsApi from "./organizationsApi";
import type { OrgOption } from "./organizationsApi";
import type { Chain } from "./types";

export function ChainsListPage() {
  const { refreshTenants } = useAuth();
  const [chains, setChains] = useState<Chain[]>([]);
  const [loading, setLoading] = useState(true);

  const [orgs, setOrgs] = useState<OrgOption[]>([]);

  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [addForm] = Form.useForm();

  const [statusModal, setStatusModal] = useState<{
    open: boolean;
    chain: Chain | null;
  }>({ open: false, chain: null });
  const [statusLoading, setStatusLoading] = useState(false);

  async function loadChains() {
    setLoading(true);
    try {
      const data = await chainsApi.list();
      setChains(data);
    } catch {
      toast.error("Failed to load chains");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadChains();
    organizationsApi.list().then(setOrgs).catch(() => setOrgs([]));
  }, []);

  async function handleAddChain(values: { name: string; timezone?: string; currency?: string; organizationId?: string }) {
    setAddLoading(true);
    try {
      const created = await chainsApi.create(values);
      setAddModalOpen(false);
      addForm.resetFields();
      setChains((prev) => [created, ...prev]);
      await refreshTenants();
      toast.success("Chain created");
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Failed to create chain";
      toast.error(message);
    } finally {
      setAddLoading(false);
    }
  }

  async function handleStatusToggleConfirm() {
    if (!statusModal.chain) return;
    const chain = statusModal.chain;
    const newStatus = chain.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    setStatusLoading(true);
    try {
      const updated = await chainsApi.updateStatus(chain.id, newStatus);
      setStatusModal({ open: false, chain: null });
      setChains((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
      await refreshTenants();
      toast.success(`Chain ${newStatus === "ACTIVE" ? "activated" : "deactivated"}`);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Failed to update status";
      toast.error(message);
    } finally {
      setStatusLoading(false);
    }
  }

  const columns = [
    {
      title: "Name",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      render: (status: string) => (
        <Tag color={status === "ACTIVE" ? "green" : "default"}>{status}</Tag>
      ),
    },
    {
      title: "Timezone",
      dataIndex: "timezone",
      key: "timezone",
      render: (v: string | null) => v ?? "—",
    },
    {
      title: "Currency",
      dataIndex: "currency",
      key: "currency",
      render: (v: string | null) => v ?? "—",
    },
    {
      title: "Created At",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
    {
      title: "Actions",
      key: "actions",
      render: (_: unknown, record: Chain) => (
        <Button
          variant="link"
          onClick={() => setStatusModal({ open: true, chain: record })}
        >
          {record.status === "ACTIVE" ? "Deactivate" : "Activate"}
        </Button>
      ),
    },
  ];

  return (
    <AppLayout title="Chain Management" breadcrumb="Chain Management">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <Typography.Title level={5} style={{ margin: 0 }}>
          Manage hotel chains
        </Typography.Title>
        <Button variant="primary" onClick={() => setAddModalOpen(true)}>
          Add Chain
        </Button>
      </div>

      {loading ? (
        <Spinner />
      ) : (
        <Table dataSource={chains} columns={columns} rowKey="id" />
      )}

      {/* Add Chain Modal */}
      <Modal
        title="Add Chain"
        open={addModalOpen}
        onCancel={() => {
          setAddModalOpen(false);
          addForm.resetFields();
        }}
        footer={null}
      >
        <Form form={addForm} layout="vertical" onFinish={handleAddChain}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: "Name is required" }]}
          >
            <Input placeholder="e.g. Marriott Hotels" />
          </Form.Item>
          <Form.Item name="timezone" label="Timezone">
            <Input placeholder="e.g. UTC" />
          </Form.Item>
          <Form.Item name="currency" label="Currency">
            <Input placeholder="e.g. USD" />
          </Form.Item>
          <Form.Item name="organizationId" label="Organisation">
            <Select
              placeholder="Select organisation (optional)"
              allowClear
              options={orgs.map((o) => ({ value: o.id, label: o.name }))}
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
            <Button variant="primary" htmlType="submit" loading={addLoading}>
              {addLoading ? "Creating…" : "Create Chain"}
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Toggle Status Confirmation Modal */}
      <Modal
        title={statusModal.chain?.status === "ACTIVE" ? "Deactivate Chain" : "Activate Chain"}
        open={statusModal.open}
        onOk={handleStatusToggleConfirm}
        onCancel={() => setStatusModal({ open: false, chain: null })}
        okText={statusModal.chain?.status === "ACTIVE" ? "Deactivate" : "Activate"}
        okButtonProps={{ loading: statusLoading }}
      >
        <p>
          Are you sure you want to{" "}
          <strong>{statusModal.chain?.status === "ACTIVE" ? "deactivate" : "activate"}</strong>{" "}
          chain <strong>{statusModal.chain?.name}</strong>?
        </p>
      </Modal>
    </AppLayout>
  );
}
