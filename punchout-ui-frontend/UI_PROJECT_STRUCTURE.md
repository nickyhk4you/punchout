# Punchout UI Frontend - Project Structure Guide

## Overview
This document provides a comprehensive guide to the frontend project structure, coding conventions, and best practices for maintaining the codebase.

## Directory Structure

```
src/
├── app/                          # Next.js app router pages
│   ├── page.tsx                  # Home/Dashboard page
│   ├── layout.tsx                # Root layout
│   ├── sessions/                 # Sessions pages
│   │   ├── page.tsx             # Sessions list
│   │   └── [sessionKey]/        # Session details
│   ├── orders/                   # Orders pages
│   │   ├── page.tsx             # Orders list
│   │   └── [orderId]/           # Order details
│   ├── developer/                # Developer tools
│   │   └── punchout/            # Punchout testing
│   ├── converter/                # cXML converter
│   └── login/                    # Login page
│
├── components/                   # Reusable React components
│   ├── ui/                      # Generic UI components
│   │   ├── LoadingSpinner.tsx   # Loading state
│   │   ├── ErrorMessage.tsx     # Error display
│   │   ├── EmptyState.tsx       # Empty state display
│   │   ├── PageHeader.tsx       # Page hero section
│   │   ├── Card.tsx             # Card containers
│   │   └── index.ts             # Barrel export
│   ├── session/                 # Session-specific components
│   │   ├── SessionInfoCard.tsx
│   │   ├── OrderObjectCard.tsx
│   │   └── NetworkRequestsTable.tsx
│   ├── networkRequest/          # Network request components
│   │   ├── RequestOverview.tsx
│   │   ├── HeadersDisplay.tsx
│   │   └── BodyDisplay.tsx
│   ├── NavBar.tsx               # Navigation bar
│   ├── Sidebar.tsx              # Sidebar navigation
│   ├── Breadcrumb.tsx           # Breadcrumb navigation
│   ├── Pagination.tsx           # Pagination component
│   └── Dashboard.tsx            # Dashboard component
│
├── lib/                         # Library code and utilities
│   ├── api.ts                   # API client and endpoints
│   ├── types.ts                 # Legacy types (deprecated)
│   └── utils/                   # Utility functions
│       ├── formatters.ts        # Date, currency, number formatters
│       ├── constants.ts         # App-wide constants
│       ├── badges.tsx           # Badge utilities and components
│       └── index.ts             # Barrel export
│
├── types/                       # TypeScript type definitions
│   └── index.ts                 # All type definitions
│
└── styles/                      # Global styles
    └── globals.css              # Tailwind and global CSS
```

## Code Organization Principles

### 1. **Separation of Concerns**
- **Pages** (`app/`) - Route handling and page-level logic
- **Components** (`components/`) - Reusable UI components
- **Utils** (`lib/utils/`) - Pure utility functions
- **Types** (`types/`) - TypeScript interfaces and types
- **API** (`lib/api.ts`) - API communication

### 2. **Component Organization**
- **UI Components** (`components/ui/`) - Generic, reusable components
- **Feature Components** (`components/session/`, `components/networkRequest/`) - Feature-specific components
- **Layout Components** (`components/NavBar.tsx`, etc.) - Layout and navigation

### 3. **Naming Conventions**

#### Files
- **Components**: PascalCase (e.g., `PageHeader.tsx`)
- **Utilities**: camelCase (e.g., `formatters.ts`)
- **Constants**: camelCase (e.g., `constants.ts`)
- **Pages**: lowercase with Next.js conventions (e.g., `page.tsx`, `[id]/page.tsx`)

#### Variables & Functions
- **Components**: PascalCase (e.g., `function PageHeader()`)
- **Functions**: camelCase (e.g., `formatDate()`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `STATUS_COLORS`)
- **Types/Interfaces**: PascalCase (e.g., `interface User`)

## Utility Functions

### Formatters (`lib/utils/formatters.ts`)

Centralized formatting functions:

```typescript
import { formatDate, formatCurrency, formatDateTime } from '@/lib/utils';

// Format dates
formatDate('2025-11-11');              // "Nov 11, 2025"
formatDateTime('2025-11-11T10:30:00'); // "Nov 11, 2025, 10:30 AM"

// Format currency
formatCurrency(1250.50, 'USD');        // "$1,250.50"

// Format numbers
formatNumber(1000000);                 // "1,000,000"

// Format duration
formatDuration(1500);                  // "1.5s"
```

### Constants (`lib/utils/constants.ts`)

Application-wide constants:

```typescript
import { STATUS_COLORS, ENVIRONMENT_COLORS } from '@/lib/utils';

// Use predefined badge colors
const className = STATUS_COLORS['CONFIRMED']; // "bg-green-100 text-green-800"
```

### Badge Components (`lib/utils/badges.tsx`)

Reusable badge components:

```typescript
import { StatusBadge, EnvironmentBadge, DirectionBadge } from '@/lib/utils';

<StatusBadge status="CONFIRMED" />
<EnvironmentBadge environment="PRODUCTION" />
<DirectionBadge direction="INBOUND" />
```

## UI Components

### Generic UI Components (`components/ui/`)

#### LoadingSpinner
```typescript
import { LoadingSpinner } from '@/components/ui';

<LoadingSpinner message="Loading data..." />
```

#### ErrorMessage
```typescript
import { ErrorMessage } from '@/components/ui';

<ErrorMessage message="Failed to load data" />
```

#### EmptyState
```typescript
import { EmptyState } from '@/components/ui';

<EmptyState 
  icon="fa-inbox"
  title="No sessions found"
  description="Try adjusting your filters"
/>
```

#### PageHeader
```typescript
import { PageHeader } from '@/components/ui';

<PageHeader
  icon="fa-list"
  title="PunchOut Sessions"
  description="View and analyze all sessions"
  gradient="from-blue-600 to-purple-600"
/>
```

#### Card Components
```typescript
import { Card, CardHeader, CardWithHeader } from '@/components/ui';

// Basic card
<Card>
  <h2>Title</h2>
  <p>Content</p>
</Card>

// Card with header
<CardWithHeader
  icon="fa-info-circle"
  iconColor="text-blue-600"
  title="Session Information"
  subtitle="View session details"
>
  <p>Content</p>
</CardWithHeader>
```

### Pagination
```typescript
import Pagination from '@/components/Pagination';

<Pagination
  currentPage={currentPage}
  totalPages={totalPages}
  totalItems={totalItems}
  itemsPerPage={itemsPerPage}
  onPageChange={handlePageChange}
  onItemsPerPageChange={handleItemsPerPageChange}
/>
```

## Best Practices

### 1. **Use Utility Functions**

❌ **Don't Repeat:**
```typescript
// In multiple components
const formatDate = (dateString?: string) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString();
};
```

✅ **Do Import:**
```typescript
import { formatDateTime } from '@/lib/utils';

const formattedDate = formatDateTime(session.sessionDate);
```

### 2. **Use Constants**

❌ **Don't Hardcode:**
```typescript
<span className="bg-green-100 text-green-800">CONFIRMED</span>
```

✅ **Do Use Constants:**
```typescript
import { StatusBadge } from '@/lib/utils';

<StatusBadge status="CONFIRMED" />
```

### 3. **Use UI Components**

❌ **Don't Recreate:**
```typescript
<div className="p-12 text-center">
  <i className="fas fa-inbox text-gray-300 text-5xl"></i>
  <p>No data found</p>
</div>
```

✅ **Do Reuse:**
```typescript
import { EmptyState } from '@/components/ui';

<EmptyState title="No data found" />
```

### 4. **Proper Type Imports**

✅ **Always import types:**
```typescript
import { PunchOutSession, Order } from '@/types';
```

### 5. **Component Structure**

```typescript
'use client'; // If using client-side hooks

import React, { useState, useEffect } from 'react';
import { SomeType } from '@/types';
import { formatDate } from '@/lib/utils';
import { PageHeader, Card } from '@/components/ui';

export default function MyPage() {
  // State
  const [data, setData] = useState<SomeType[]>([]);
  const [loading, setLoading] = useState(true);

  // Effects
  useEffect(() => {
    loadData();
  }, []);

  // Functions
  const loadData = async () => {
    // ...
  };

  // Render loading
  if (loading) return <LoadingSpinner />;

  // Render component
  return (
    <div>
      <PageHeader title="My Page" />
      <Card>
        {/* Content */}
      </Card>
    </div>
  );
}
```

### 6. **Error Handling**

```typescript
import { useState } from 'react';
import { ErrorMessage } from '@/components/ui';

const [error, setError] = useState<string | null>(null);

try {
  // API call
} catch (err: any) {
  setError(err.message || 'An error occurred');
}

// In render
{error && <ErrorMessage message={error} />}
```

## API Usage

### Import API Functions
```typescript
import { sessionAPI, orderAPI, orderAPIv2 } from '@/lib/api';
```

### Use with Error Handling
```typescript
const loadSessions = async () => {
  try {
    setLoading(true);
    setError(null);
    const data = await sessionAPI.getAllSessions(filters);
    setSessions(data);
  } catch (err: any) {
    setError(err.message || 'Failed to load sessions');
  } finally {
    setLoading(false);
  }
};
```

## Styling Guidelines

### 1. **Use Tailwind Classes**
- Prefer Tailwind utility classes over custom CSS
- Use consistent spacing: `px-4`, `py-6`, `mb-4`, etc.
- Use consistent colors from Tailwind palette

### 2. **Responsive Design**
```typescript
className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3"
```

### 3. **Consistent Card Styling**
```typescript
className="bg-white rounded-xl shadow-lg border border-gray-200 p-6"
```

### 4. **Button Styling**
```typescript
// Primary button
className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"

// Secondary button
className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
```

## Performance Considerations

### 1. **Pagination**
Always paginate large lists (use `Pagination` component)

### 2. **Memoization**
Use `useMemo` and `useCallback` for expensive operations

### 3. **Conditional Rendering**
```typescript
{loading && <LoadingSpinner />}
{error && <ErrorMessage message={error} />}
{!loading && !error && data.length === 0 && <EmptyState />}
{!loading && !error && data.length > 0 && <DataTable />}
```

## File Imports Order

```typescript
// 1. React imports
import React, { useState, useEffect } from 'react';

// 2. Next.js imports
import { useRouter } from 'next/navigation';
import Link from 'next/link';

// 3. Type imports
import { PunchOutSession } from '@/types';

// 4. API imports
import { sessionAPI } from '@/lib/api';

// 5. Utility imports
import { formatDate, StatusBadge } from '@/lib/utils';

// 6. Component imports
import { PageHeader, Card } from '@/components/ui';
import Pagination from '@/components/Pagination';
```

## Testing

### Component Testing
```typescript
// Use React Testing Library
import { render, screen } from '@testing-library/react';
import MyComponent from './MyComponent';

test('renders component', () => {
  render(<MyComponent />);
  expect(screen.getByText('Hello')).toBeInTheDocument();
});
```

## Common Patterns

### List Pages
1. Use `PageHeader` for hero section
2. Use `Card` for filters
3. Use `Card` for table/list
4. Use `Pagination` for pagination
5. Use `EmptyState` for no results
6. Use `LoadingSpinner` for loading
7. Use `ErrorMessage` for errors

### Detail Pages
1. Use `PageHeader` for hero section
2. Use `Breadcrumb` for navigation
3. Use `CardWithHeader` for sections
4. Use formatted data with utility functions
5. Use badge components for status/tags

## Migration Guide

### Migrating Existing Code

1. **Replace inline formatting:**
```typescript
// Before
formatDate(session.sessionDate) // local function

// After
import { formatDateTime } from '@/lib/utils';
formatDateTime(session.sessionDate)
```

2. **Replace badge logic:**
```typescript
// Before
<span className={getStatusClass(status)}>{status}</span>

// After
import { StatusBadge } from '@/lib/utils';
<StatusBadge status={status} />
```

3. **Replace cards:**
```typescript
// Before
<div className="bg-white rounded-xl shadow-lg p-6">
  <h2>Title</h2>
  ...
</div>

// After
import { CardWithHeader } from '@/components/ui';
<CardWithHeader title="Title">...</CardWithHeader>
```

## Summary

This structure provides:
✅ **Reusability** - Shared components and utilities
✅ **Maintainability** - Clear organization and conventions
✅ **Consistency** - Standard patterns across pages
✅ **Type Safety** - TypeScript throughout
✅ **Performance** - Optimized patterns (pagination, memoization)
✅ **Developer Experience** - Easy to find and use code

For questions or improvements, consult this guide and maintain these patterns!
