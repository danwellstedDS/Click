import { useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

export function TenantPickerPage() {
  const { tenants, switchTenant } = useAuth();
  const navigate = useNavigate();

  async function handleSelect(tenantId: string) {
    await switchTenant(tenantId);
    navigate("/");
  }

  return (
    <div style={{ maxWidth: 400, margin: "80px auto", fontFamily: "system-ui", padding: 24 }}>
      <h1>Select Tenant</h1>
      <p>You belong to multiple tenants. Choose one to continue:</p>
      <ul style={{ listStyle: "none", padding: 0 }}>
        {tenants.map((t) => (
          <li key={t.tenantId} style={{ marginBottom: 8 }}>
            <button
              onClick={() => handleSelect(t.tenantId)}
              style={{ width: "100%", padding: 10, cursor: "pointer" }}
            >
              {t.tenantId} ({t.role})
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
