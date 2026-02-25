import { Tabs } from "@derbysoft/neat-design";
import { AppLayout } from "../../components/AppLayout";
import { PortfolioOverviewTab }  from "./tabs/PortfolioOverviewTab";
import { CapitalAllocationTab }  from "./tabs/CapitalAllocationTab";
import { MarginalReturnTab }     from "./tabs/MarginalReturnTab";
import { RiskVolatilityTab }     from "./tabs/RiskVolatilityTab";
import { CannibalassationTab }   from "./tabs/CannibalassationTab";
import { PaceForecastTab }       from "./tabs/PaceForecastTab";

const TAB_ITEMS = [
  {
    key: "overview",
    label: "Portfolio Overview",
    children: <PortfolioOverviewTab />,
  },
  {
    key: "capital",
    label: "Capital Allocation",
    children: <CapitalAllocationTab />,
  },
  {
    key: "marginal",
    label: "Marginal Return",
    children: <MarginalReturnTab />,
  },
  {
    key: "risk",
    label: "Risk & Volatility",
    children: <RiskVolatilityTab />,
  },
  {
    key: "cannibalisation",
    label: "Cannibalisation",
    children: <CannibalassationTab />,
  },
  {
    key: "pace",
    label: "Pace & Forecast",
    children: <PaceForecastTab />,
  },
];

export function DashboardPage() {
  return (
    <AppLayout title="Portfolio Intelligence Dashboard" breadcrumb="Dashboard">
      <Tabs defaultActiveKey="overview" items={TAB_ITEMS} />
    </AppLayout>
  );
}
