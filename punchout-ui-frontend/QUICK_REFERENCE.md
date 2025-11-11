# Quick Reference Guide

## 🚀 Common Tasks

### Import Utilities
```typescript
import { formatDate, formatCurrency, StatusBadge } from '@/lib/utils';
```

### Import UI Components
```typescript
import { PageHeader, Card, EmptyState, LoadingSpinner } from '@/components/ui';
```

### Format Data
```typescript
formatDate('2025-11-11');              // "Nov 11, 2025"
formatDateTime('2025-11-11T10:30:00'); // "Nov 11, 2025, 10:30 AM"
formatCurrency(1250.50);               // "$1,250.50"
formatDuration(1500);                  // "1.5s"
```

### Display Badges
```typescript
import { StatusBadge, EnvironmentBadge } from '@/lib/utils';

<StatusBadge status="CONFIRMED" />
<EnvironmentBadge environment="PRODUCTION" />
```

### Page Structure
```typescript
import { PageHeader, Card } from '@/components/ui';

<PageHeader
  icon="fa-list"
  title="My Page"
  description="Page description"
/>

<Card>
  {/* Content */}
</Card>
```

### Empty State
```typescript
import { EmptyState } from '@/components/ui';

<EmptyState 
  icon="fa-inbox"
  title="No data found"
  description="Try adjusting your filters"
/>
```

### Loading State
```typescript
import { LoadingSpinner } from '@/components/ui';

{loading && <LoadingSpinner message="Loading data..." />}
```

### Error State
```typescript
import { ErrorMessage } from '@/components/ui';

{error && <ErrorMessage message={error} />}
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

## 📁 File Locations

- **Utilities**: `src/lib/utils/`
- **UI Components**: `src/components/ui/`
- **Types**: `src/types/index.ts`
- **API**: `src/lib/api.ts`
- **Pages**: `src/app/`

## 📚 Full Documentation

See **UI_PROJECT_STRUCTURE.md** for complete guide!
