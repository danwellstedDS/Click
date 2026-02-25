import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Button,
  Form,
  Input,
  Modal,
  Spinner,
  Switch,
  Table,
  Tag,
  toast,
  Typography,
} from "@derbysoft/neat-design";
import { AppLayout } from "../../components/AppLayout";
import * as propertiesApi from "./propertiesApi";
import type { CreatePropertyRequest, PropertyListItem } from "./types";

export function PropertiesListPage() {
  const navigate = useNavigate();
  const [properties, setProperties] = useState<PropertyListItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [addForm] = Form.useForm();

  const [deleteModal, setDeleteModal] = useState<{
    open: boolean;
    property: PropertyListItem | null;
  }>({ open: false, property: null });
  const [deleteLoading, setDeleteLoading] = useState(false);

  async function loadProperties() {
    setLoading(true);
    try {
      const data = await propertiesApi.listProperties();
      setProperties(data);
    } catch {
      toast.error("Failed to load properties");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProperties();
  }, []);

  async function handleAddProperty(values: CreatePropertyRequest) {
    setAddLoading(true);
    try {
      await propertiesApi.createProperty(values);
      setAddModalOpen(false);
      addForm.resetFields();
      toast.success("Property created");
      await loadProperties();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Failed to create property";
      toast.error(message);
    } finally {
      setAddLoading(false);
    }
  }

  async function handleDeleteConfirm() {
    if (!deleteModal.property) return;
    setDeleteLoading(true);
    try {
      await propertiesApi.deleteProperty(deleteModal.property.id);
      setDeleteModal({ open: false, property: null });
      setProperties((prev) => prev.filter((p) => p.id !== deleteModal.property!.id));
      toast.success("Property deleted");
    } catch {
      toast.error("Failed to delete property");
    } finally {
      setDeleteLoading(false);
    }
  }

  const columns = [
    {
      title: "Name",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "Active",
      dataIndex: "isActive",
      key: "isActive",
      render: (isActive: boolean) => (
        <Tag color={isActive ? "green" : "default"}>{isActive ? "Active" : "Inactive"}</Tag>
      ),
    },
    {
      title: "External Ref",
      dataIndex: "externalPropertyRef",
      key: "externalPropertyRef",
      render: (ref: string | null) => ref ?? "—",
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
      render: (_: unknown, record: PropertyListItem) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button
            variant="link"
            onClick={() => navigate(`/properties/${record.id}`)}
          >
            View
          </Button>
          <Button
            variant="link"
            onClick={() => setDeleteModal({ open: true, property: record })}
          >
            Delete
          </Button>
        </div>
      ),
    },
  ];

  return (
    <AppLayout title="Properties" breadcrumb="Properties">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <Typography.Title level={5} style={{ margin: 0 }}>
          Manage tenant properties
        </Typography.Title>
        <Button variant="primary" onClick={() => setAddModalOpen(true)}>
          Add Property
        </Button>
      </div>

      {loading ? (
        <Spinner />
      ) : (
        <Table dataSource={properties} columns={columns} rowKey="id" />
      )}

      {/* Add Property Modal */}
      <Modal
        title="Add Property"
        open={addModalOpen}
        onCancel={() => {
          setAddModalOpen(false);
          addForm.resetFields();
        }}
        footer={null}
      >
        <Form
          form={addForm}
          layout="vertical"
          initialValues={{ isActive: true }}
          onFinish={handleAddProperty}
        >
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: "Name is required" }]}
          >
            <Input placeholder="Property name" />
          </Form.Item>
          <Form.Item name="isActive" label="Active" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="externalPropertyRef" label="External Property Ref">
            <Input placeholder="Optional external reference" />
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
              {addLoading ? "Creating…" : "Create Property"}
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        title="Delete Property"
        open={deleteModal.open}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModal({ open: false, property: null })}
        okText="Delete"
        okButtonProps={{ loading: deleteLoading }}
      >
        <p>
          Are you sure you want to delete{" "}
          <strong>{deleteModal.property?.name}</strong>? The property will be
          deactivated and hidden from this list.
        </p>
      </Modal>
    </AppLayout>
  );
}
