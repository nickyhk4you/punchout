/**
 * Application-wide constants
 */

// Status badge colors
export const STATUS_COLORS: Record<string, string> = {
  RECEIVED: 'bg-blue-100 text-blue-800',
  CONFIRMED: 'bg-green-100 text-green-800',
  PROCESSING: 'bg-yellow-100 text-yellow-800',
  FAILED: 'bg-red-100 text-red-800',
  PENDING: 'bg-gray-100 text-gray-800',
};

// Operation badge colors
export const OPERATION_COLORS: Record<string, string> = {
  CREATE: 'bg-green-100 text-green-800',
  EDIT: 'bg-yellow-100 text-yellow-800',
  INSPECT: 'bg-blue-100 text-blue-800',
  DELETE: 'bg-red-100 text-red-800',
};

// Environment badge colors
export const ENVIRONMENT_COLORS: Record<string, string> = {
  PRODUCTION: 'bg-red-100 text-red-800',
  STAGING: 'bg-orange-100 text-orange-800',
  DEVELOPMENT: 'bg-gray-100 text-gray-800',
  TEST: 'bg-blue-100 text-blue-800',
};

// Direction badge colors
export const DIRECTION_COLORS: Record<string, string> = {
  INBOUND: 'bg-blue-100 text-blue-800',
  OUTBOUND: 'bg-purple-100 text-purple-800',
};

// Success/Error badge colors
export const SUCCESS_COLORS = {
  SUCCESS: 'bg-green-100 text-green-800',
  ERROR: 'bg-red-100 text-red-800',
};

// Pagination options
export const ITEMS_PER_PAGE_OPTIONS = [10, 25, 50, 100];
export const DEFAULT_ITEMS_PER_PAGE = 25;

// API timeouts (in milliseconds)
export const API_TIMEOUT = 10000; // 10 seconds
export const LONG_API_TIMEOUT = 30000; // 30 seconds

// Date format options
export const DATE_FORMAT_OPTIONS: Intl.DateTimeFormatOptions = {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
};

export const DATETIME_FORMAT_OPTIONS: Intl.DateTimeFormatOptions = {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
};

// Table column widths
export const TABLE_COLUMN_WIDTHS = {
  SMALL: 'w-1/12',
  MEDIUM: 'w-1/6',
  LARGE: 'w-1/4',
  XLARGE: 'w-1/3',
};

// Loading messages
export const LOADING_MESSAGES = {
  SESSIONS: 'Loading sessions...',
  ORDERS: 'Loading orders...',
  REQUESTS: 'Loading network requests...',
  DEFAULT: 'Loading...',
};

// Error messages
export const ERROR_MESSAGES = {
  NETWORK: 'Network error. Please check your connection.',
  NOT_FOUND: 'Resource not found.',
  UNAUTHORIZED: 'You are not authorized to access this resource.',
  SERVER: 'Server error. Please try again later.',
  DEFAULT: 'An error occurred. Please try again.',
};

// Empty state messages
export const EMPTY_MESSAGES = {
  NO_SESSIONS: 'No sessions found',
  NO_ORDERS: 'No orders found',
  NO_REQUESTS: 'No network requests found',
  NO_RESULTS: 'No results found',
};
