/**
 * Utility functions for formatting data
 */

/**
 * Format a date string to a localized string
 * @param dateString - ISO date string or undefined
 * @param options - Intl.DateTimeFormatOptions for customization
 * @returns Formatted date string or '-' if no date
 */
export function formatDate(
  dateString?: string,
  options?: Intl.DateTimeFormatOptions
): string {
  if (!dateString) return '-';
  
  const defaultOptions: Intl.DateTimeFormatOptions = {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    ...options,
  };
  
  return new Date(dateString).toLocaleDateString('en-US', defaultOptions);
}

/**
 * Format a date string to a localized date and time string
 * @param dateString - ISO date string or undefined
 * @returns Formatted date/time string or '-' if no date
 */
export function formatDateTime(dateString?: string): string {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('en-US');
}

/**
 * Format a time from a date string
 * @param dateString - ISO date string
 * @returns Formatted time string
 */
export function formatTime(dateString: string): string {
  return new Date(dateString).toLocaleTimeString('en-US');
}

/**
 * Format a number as currency
 * @param amount - The amount to format
 * @param currency - Currency code (default: USD)
 * @returns Formatted currency string or '-' if no amount
 */
export function formatCurrency(
  amount?: number,
  currency: string = 'USD'
): string {
  if (amount === undefined || amount === null) return '-';
  
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount);
}

/**
 * Format a number with thousand separators
 * @param num - Number to format
 * @returns Formatted number string
 */
export function formatNumber(num: number): string {
  return new Intl.NumberFormat('en-US').format(num);
}

/**
 * Truncate a string to a maximum length
 * @param str - String to truncate
 * @param maxLength - Maximum length
 * @param ellipsis - Ellipsis string (default: '...')
 * @returns Truncated string
 */
export function truncate(
  str: string,
  maxLength: number,
  ellipsis: string = '...'
): string {
  if (str.length <= maxLength) return str;
  return str.substring(0, maxLength - ellipsis.length) + ellipsis;
}

/**
 * Format bytes to human readable format
 * @param bytes - Number of bytes
 * @param decimals - Number of decimal places
 * @returns Formatted string (e.g., "1.5 MB")
 */
export function formatBytes(bytes: number, decimals: number = 2): string {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

/**
 * Format duration in milliseconds to human readable format
 * @param ms - Duration in milliseconds
 * @returns Formatted string (e.g., "1.5s" or "500ms")
 */
export function formatDuration(ms: number): string {
  if (ms < 1000) {
    return `${ms}ms`;
  }
  
  const seconds = ms / 1000;
  if (seconds < 60) {
    return `${seconds.toFixed(1)}s`;
  }
  
  const minutes = seconds / 60;
  return `${minutes.toFixed(1)}m`;
}
