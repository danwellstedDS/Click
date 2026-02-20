import { useNavigate } from "react-router-dom";
import { Button } from "@derbysoft/neat-design";
import { useAuth } from "./AuthContext";

export function TenantPickerPage() {
  const { tenants, switchTenant } = useAuth();
  const navigate = useNavigate();

  async function handleSelect(tenantId: string) {
    await switchTenant(tenantId);
    navigate("/");
  }

  return (
    <div style={{ maxWidth: 400, margin: "80px auto", padding: 24 }}>
      <h1>Select Tenant</h1>
      <p>You belong to multiple tenants. Choose one to continue:</p>
      {tenants.map((t) => (
        <div key={t.tenantId} style={{ marginBottom: 8 }}>
          <Button
            variant="secondary"
            block
            onClick={() => handleSelect(t.tenantId)}
          >
            {t.tenantId} ({t.role})
          </Button>
        </div>
      ))}
    </div>
  );
}
