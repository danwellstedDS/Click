---
description: Frontend development standards, best practices, and conventions for the LTI React application including component patterns, state management, UI/UX guidelines, and testing practices
globs: ["apps/web/src/**/*.{js,jsx,ts,tsx}", "apps/web/cypress/**/*.{ts,js}", "apps/web/tsconfig.json", "apps/web/cypress.config.ts", "apps/web/package.json"]
alwaysApply: true
---

# Frontend Project Configuration and Best Practices

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
  - [Core Technologies](#core-technologies)
  - [UI Framework](#ui-framework)
  - [State Management & Data Flow](#state-management--data-flow)
  - [Testing Framework](#testing-framework)
  - [Development Tools](#development-tools)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
  - [Language and Naming Conventions](#language-and-naming-conventions)
  - [Component Conventions](#component-conventions)
  - [State Management](#state-management)
  - [Service Layer Architecture](#service-layer-architecture)
- [UI/UX Standards](#uiux-standards)
  - [Neat Design Integration](#neat-design-integration)
  - [Form Handling](#form-handling)
  - [Navigation Patterns](#navigation-patterns)
  - [Accessibility](#accessibility)
- [Testing Standards](#testing-standards)
  - [End-to-End Testing with Cypress](#end-to-end-testing-with-cypress)
  - [Test Organization](#test-organization)
- [Configuration Standards](#configuration-standards)
  - [TypeScript Configuration](#typescript-configuration)
  - [ESLint Configuration](#eslint-configuration)
  - [Environment Configuration](#environment-configuration)
- [Performance Best Practices](#performance-best-practices)
  - [Component Optimization](#component-optimization)
  - [Bundle Optimization](#bundle-optimization)
  - [API Efficiency](#api-efficiency)
- [Development Workflow](#development-workflow)
  - [Git Workflow](#git-workflow)
  - [Development Scripts](#development-scripts)
  - [Code Quality](#code-quality)
- [Migration Strategy](#migration-strategy)
  - [TypeScript Migration](#typescript-migration)
  - [Component Modernization](#component-modernization)

---

## Overview

This document outlines the best practices, conventions, and standards used in the LTI frontend application. These practices ensure code consistency, maintainability, and optimal development experience.

## Technology Stack

### Core Technologies
- **React 18.3.1**: Modern React with functional components and hooks
- **TypeScript 5.5.4**: For type safety and better development experience
- **Vite 5.4.1**: Build tooling and development server
- **React Router DOM 7.13.0**: Client-side routing and navigation

### UI Framework
- **@derbysoft/neat-design ^2.2.3**: Design system (wraps Ant Design v5.26+) — the only UI component library
- **@derbysoft/neat-design-icons**: Icon set

Note: neat-design ships DatePicker, Select, Cascader, TimePicker, and other form primitives internally — no separate packages needed.

### State Management & Data Flow
- **React Hooks**: useState, useEffect for local state management
- **Context API**: For shared state across the component tree
- **Native fetch** via the project's `lib/apiClient.ts` wrapper — all API calls go through `apiRequest<T>(path, options)`

### Testing Framework
- **Cypress 14.4.1**: End-to-end testing
- **React Testing Library**: Component testing utilities

### Development Tools
- **ESLint**: Code linting with React-specific rules
- **TypeScript**: Static type checking
- **Web Vitals**: Performance monitoring

## Project Structure

```
apps/web/
├── src/
│   ├── features/          # Feature-sliced directories (e.g. auth/)
│   │   └── <feature>/
│   │       ├── <Feature>Page.tsx
│   │       ├── <feature>Api.ts
│   │       └── types.ts
│   ├── lib/               # Shared utilities (apiClient.ts)
│   ├── App.tsx
│   └── main.tsx
├── package.json
└── vite.config.ts
```

## Coding Standards

### Naming Conventions

- **Component Naming**: Use PascalCase for React components (e.g., `CandidateCard`, `PositionDetails`, `RecruiterDashboard`)
- **Variable Naming**: Use camelCase for variables and functions (e.g., `candidateId`, `handleSubmit`, `fetchPositions`)
- **Constants Naming**: Use UPPER_SNAKE_CASE for constants (e.g., `MAX_CANDIDATES_PER_PAGE`, `API_BASE_URL`)
- **Type/Interface Naming**: Use PascalCase for types and interfaces (e.g., `CandidateData`, `PositionProps`, `ICandidateService`)
- **File Naming**: Use PascalCase for component files (e.g., `CandidateCard.tsx`, `PositionDetails.tsx`) and camelCase for utility files (e.g., `candidateService.js`, `apiUtils.ts`)
- **CSS Class Naming**: Use kebab-case for CSS classes (e.g., `candidate-card`, `position-details`)
- **Hook Naming**: Use camelCase starting with "use" prefix (e.g., `useCandidate`, `usePositionData`, `useFormValidation`)

**Examples:**

```typescript
// Good: All in English
import React, { useState, useEffect } from 'react';

type CandidateCardProps = {
    candidate: Candidate;
    index: number;
    onClick: (candidate: Candidate) => void;
};

const CandidateCard: React.FC<CandidateCardProps> = ({ candidate, index, onClick }) => {
    const [isLoading, setIsLoading] = useState(false);

    // Handle candidate card click event
    const handleCardClick = () => {
        onClick(candidate);
    };

    return (
        <div className="candidate-card" onClick={handleCardClick}>
            {/* Component JSX */}
        </div>
    );
};

// Avoid: Non-English comments or names
const TarjetaCandidato: React.FC<PropsTarjetaCandidato> = ({ candidato, indice, alHacerClic }) => {
    const [estaCargando, setEstaCargando] = useState(false);

    // Manejar evento de clic en la tarjeta de candidato
    const manejarClicTarjeta = () => {
        alHacerClic(candidato);
    };

    return (
        <div className="tarjeta-candidato" onClick={manejarClicTarjeta}>
            {/* JSX del componente */}
        </div>
    );
};
```

**Error Messages and Console Logs:**

```typescript
// Good: English error messages
catch (error) {
    console.error('Failed to fetch candidates:', error);
    setError('Unable to load candidates. Please try again later.');
}

// Avoid: Non-English messages
catch (error) {
    console.error('Error al obtener candidatos:', error);
    setError('No se pudieron cargar los candidatos. Por favor, inténtelo de nuevo más tarde.');
}
```

**Service Layer Examples:**

```typescript
// Good: English naming in services
export const candidateService = {
    getAllCandidates: async () => {
        try {
            const data = await apiRequest<Candidate[]>('/candidates');
            return data;
        } catch (error) {
            console.error('Error fetching candidates:', error);
            throw error;
        }
    }
};

// Avoid: Non-English naming
export const servicioCandidatos = {
    obtenerTodosLosCandidatos: async () => {
        try {
            const respuesta = await apiRequest<Candidate[]>('/candidates');
            return respuesta;
        } catch (error) {
            console.error('Error al obtener candidatos:', error);
            throw error;
        }
    }
};
```

### Component Conventions

#### Functional Components
- **Always use functional components** with hooks instead of class components
- Use **TypeScript for new components** when possible
- Keep **JavaScript for legacy components** until migration

```typescript
// Preferred - TypeScript functional component
import React, { useState, useEffect } from 'react';

type Position = {
    id: number;
    title: string;
    status: 'Open' | 'Contratado' | 'Cerrado' | 'Borrador';
};

const Positions: React.FC = () => {
    const [positions, setPositions] = useState<Position[]>([]);
    // Component logic
};
```

#### Component Props
- **Define TypeScript interfaces** for component props when using TypeScript
- Use **destructuring** for props
- Include **default values** where appropriate

```typescript
type CandidateCardProps = {
    candidate: Candidate;
    index: number;
    onClick: (candidate: Candidate) => void;
};

const CandidateCard: React.FC<CandidateCardProps> = ({ candidate, index, onClick }) => {
    // Component implementation
};
```

### State Management

#### Local State with Hooks
- Use **useState** for component-level state
- Use **useEffect** for side effects and data fetching
- **Extract custom hooks** for reusable stateful logic

```javascript
const [formData, setFormData] = useState({
    title: '',
    description: '',
    status: 'Borrador'
});
```

#### Loading and Error States
- **Always handle loading states** for async operations
- **Implement error handling** with user-friendly messages
- Use **neat-design Spinner, Alert, and toast** components for feedback

```javascript
const [loading, setLoading] = useState(true);
const [error, setError] = useState('');

// In async function
try {
    setLoading(true);
    const data = await apiCall();
    toast.success('Operation completed successfully');
} catch (error) {
    setError('Error message: ' + error.message);
} finally {
    setLoading(false);
}
```

### Service Layer Architecture

#### API Services
- **Centralize API calls** in feature `*Api.ts` files
- Use **`apiRequest<T>`** from `lib/apiClient.ts` for all HTTP requests
- **Handle errors at service level** when appropriate

```typescript
import { apiRequest } from '@/lib/apiClient';

export const positionApi = {
    getAllPositions: async () => {
        try {
            return await apiRequest<Position[]>('/positions');
        } catch (error) {
            console.error('Error fetching positions:', error);
            throw error;
        }
    },

    updatePosition: async (id: number, positionData: Partial<Position>) => {
        try {
            return await apiRequest<Position>(`/positions/${id}`, {
                method: 'PUT',
                body: JSON.stringify(positionData),
            });
        } catch (error) {
            console.error('Error updating position:', error);
            throw error;
        }
    }
};
```

## UI/UX Standards

### Neat Design Integration

`@derbysoft/neat-design` is the only UI component library. Never use raw HTML form elements, plain divs for layout structure, or inline styles where a neat-design component exists.

#### Setup — ConfigProvider (required in main.tsx)

Wrap the entire app with neat-design's ConfigProvider:

```tsx
import { ConfigProvider } from '@derbysoft/neat-design';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <ConfigProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ConfigProvider>
    </BrowserRouter>
  </React.StrictMode>
);
```

#### Import convention

Always import from `@derbysoft/neat-design` — never from `antd` directly.

```tsx
import { Button, Form, Input, Table, Modal, Alert, Spinner } from '@derbysoft/neat-design';
```

#### Component catalogue (key components to use)

| Need | Use |
|---|---|
| Buttons | `Button` — variants: `primary`, `secondary`, `tertiary`, `tertiaryInline`, `link` |
| Text input | `Input`, `InputNumber` |
| Dropdowns | `Select`, `Cascader` |
| Dates | `DatePicker`, `TimePicker` |
| Checkboxes / Radios | `Checkbox`, `Radio`, `Switch` |
| Forms | `Form` (with `Form.Item` for layout + validation) |
| Data tables | `Table` |
| Modals | `Modal` |
| Loading | `Spinner`, `Skeleton` |
| Inline alerts | `Alert` |
| Toast notifications | `toast` (service) |
| Snack bar | `SnackBar` (service) |
| Navigation | `Menu`, `Breadcrumb`, `Tabs`, `Steps` |
| Layout | `Layout` (with `Layout.Header`, `Layout.Content`, `Layout.Sider`) |
| Overlays | `Drawer`, `Tooltip`, `Popover`, `Dropdown` |
| Tag / Badge | `Tag`, `Badge` |

### Form Handling

- Use `Form` and `Form.Item` with `name` + `rules` for validation — do not manage form state manually via `useState` per-field
- Use `loading` prop on `Button` instead of disabling + text swap
- **Clear form state** after successful submission

```tsx
import { Form, Input, Button } from '@derbysoft/neat-design';

<Form onFinish={handleSubmit} layout="vertical">
  <Form.Item name="title" label="Title" rules={[{ required: true }]}>
    <Input />
  </Form.Item>
  <Form.Item>
    <Button type="submit" variant="primary" loading={saving}>
      {saving ? 'Saving…' : 'Save'}
    </Button>
  </Form.Item>
</Form>
```

### Navigation Patterns

- Use **React Router** for all navigation
- **Implement breadcrumbs** with back navigation
- Use **programmatic navigation** with useNavigate hook

```tsx
import { useNavigate } from 'react-router-dom';
import { Button, Breadcrumb } from '@derbysoft/neat-design';

const navigate = useNavigate();

<Button variant="link" onClick={() => navigate('/')}>← Back</Button>

<Breadcrumb items={[{ title: 'Dashboard', href: '/' }, { title: 'Detail' }]} />
```

### Accessibility
- Include **aria-label** attributes for interactive elements
- Use **semantic HTML** elements
- Ensure **keyboard navigation** support
- Provide **alternative text** for images

```tsx
<Input
  placeholder="Search by title"
  aria-label="Search positions by title"
/>
```

## Testing Standards

### End-to-End Testing with Cypress
- **Test user workflows** rather than implementation details
- Use **data-testid** attributes for reliable element selection
- **Organize tests by feature** (candidates.cy.ts, positions.cy.ts)
- **Include API testing** alongside UI testing

```typescript
describe('Positions API - Update', () => {
    beforeEach(() => {
        cy.window().then((win) => {
            win.localStorage.clear();
        });
    });

    it('should update a position successfully', () => {
        const updateData = {
            title: 'Updated Test Position',
            status: 'Open'
        };

        cy.request({
            method: 'PUT',
            url: `${API_URL}/positions/${testPositionId}`,
            body: updateData
        }).then((response) => {
            expect(response.status).to.eq(200);
            expect(response.body.data.title).to.eq(updateData.title);
        });
    });
});
```

### Test Organization
- **Group related tests** with describe blocks
- **Use descriptive test names** that explain the expected behavior
- **Test both success and error scenarios**
- **Include edge cases** and validation testing

## Configuration Standards

### TypeScript Configuration
- Enable **strict mode** for type checking
- Use **path mapping** with "@/*" for cleaner imports
- Include **both Cypress and Node types**
- Configure **ES5 target** for broader compatibility

```json
{
    "compilerOptions": {
        "strict": true,
        "baseUrl": ".",
        "paths": {
            "@/*": ["src/*"]
        },
        "types": ["cypress", "node"]
    }
}
```

### ESLint Configuration
- Extend **React App** configuration
- **Automatic code formatting** and error detection
- **Consistent code style** across the project

### Environment Configuration
- Use **environment variables** for API URLs
- **Separate configurations** for development and production
- **Configure Cypress** with environment-specific settings

```javascript
// cypress.config.ts
export default defineConfig({
    e2e: {
        baseUrl: 'http://localhost:5173',
        env: {
            API_URL: 'http://localhost:3010'
        }
    }
});
```

## Performance Best Practices

### Component Optimization
- **Lazy load** components when appropriate
- **Memoize expensive calculations** with useMemo
- **Avoid unnecessary re-renders** with useCallback
- **Extract reusable logic** into custom hooks

### Bundle Optimization
- **Vite tree-shakes ES modules by default**
- **Code splitting** at route level
- **Optimize images** and static assets
- **Monitor bundle size** with build tools
- Import neat-design components individually to maximise tree-shaking:

```tsx
import { Button } from '@derbysoft/neat-design';  // ✓ tree-shakeable
```

### API Efficiency
- **Implement proper error handling** for network requests
- **Cache API responses** where appropriate
- **Use loading states** to improve perceived performance
- **Batch API calls** when possible

## Development Workflow

- **Feature Branches**: Develop features in separate branches, adding descriptive suffix "-frontend" to allow working in parallel and avoid conflicts or collisions
- **Descriptive Commits**: Write descriptive commit messages in English
- **Code Review**: Code review before merging
- **Small Branches**: Keep branches small and focused

### Development Scripts
```bash
npm run dev       # Vite dev server
npm run build     # Production build
npm run preview   # Preview production build
npm run cypress:open    # Open Cypress test runner
npm run cypress:run     # Run Cypress tests headlessly
```

### Code Quality
- **ESLint validation** before commits
- **TypeScript compilation** without errors
- **All tests passing** before deployment
- **Performance monitoring** with Web Vitals

## Migration Strategy

### TypeScript Migration
- **Gradual migration** from JavaScript to TypeScript
- **New components in TypeScript** by default
- **Maintain existing JavaScript** components until planned refactor
- **Add types incrementally** to existing code

### Component Modernization
- **Functional components** over class components
- **Hooks** instead of lifecycle methods
- **@derbysoft/neat-design components** replace all raw HTML form elements and inline styles
- New pages must use neat-design layout primitives (`Layout`, `Card`, `Divider`)
- Existing inline-styled components should be migrated to neat-design in each PR that touches them

This document serves as the foundation for maintaining code quality and consistency across the LTI frontend application. All team members should follow these practices to ensure a maintainable and scalable codebase.
