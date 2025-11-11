/**
 * Badge utility components and functions
 */

import React from 'react';
import {
  STATUS_COLORS,
  OPERATION_COLORS,
  ENVIRONMENT_COLORS,
  DIRECTION_COLORS,
  SUCCESS_COLORS,
} from './constants';

/**
 * Get status badge className
 */
export function getStatusBadgeClass(status: string): string {
  return STATUS_COLORS[status.toUpperCase()] || 'bg-gray-100 text-gray-800';
}

/**
 * Get operation badge className
 */
export function getOperationBadgeClass(operation: string): string {
  return OPERATION_COLORS[operation.toUpperCase()] || 'bg-gray-100 text-gray-800';
}

/**
 * Get environment badge className
 */
export function getEnvironmentBadgeClass(environment: string): string {
  return ENVIRONMENT_COLORS[environment.toUpperCase()] || 'bg-gray-100 text-gray-800';
}

/**
 * Get direction badge className
 */
export function getDirectionBadgeClass(direction: string): string {
  return DIRECTION_COLORS[direction.toUpperCase()] || 'bg-gray-100 text-gray-800';
}

/**
 * Get success badge className
 */
export function getSuccessBadgeClass(success: boolean): string {
  return success ? SUCCESS_COLORS.SUCCESS : SUCCESS_COLORS.ERROR;
}

interface BadgeProps {
  text: string;
  className?: string;
}

/**
 * Generic Badge component
 */
export function Badge({ text, className = '' }: BadgeProps) {
  return (
    <span
      className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${className}`}
    >
      {text}
    </span>
  );
}

/**
 * Status Badge component
 */
export function StatusBadge({ status }: { status: string }) {
  return <Badge text={status} className={getStatusBadgeClass(status)} />;
}

/**
 * Operation Badge component
 */
export function OperationBadge({ operation }: { operation: string }) {
  return <Badge text={operation} className={getOperationBadgeClass(operation)} />;
}

/**
 * Environment Badge component
 */
export function EnvironmentBadge({ environment }: { environment: string }) {
  return <Badge text={environment} className={getEnvironmentBadgeClass(environment)} />;
}

/**
 * Direction Badge component
 */
export function DirectionBadge({ direction }: { direction: string }) {
  return <Badge text={direction} className={getDirectionBadgeClass(direction)} />;
}

/**
 * Success Badge component
 */
export function SuccessBadge({ success, statusCode }: { success: boolean; statusCode?: number }) {
  return (
    <Badge
      text={statusCode?.toString() || (success ? 'SUCCESS' : 'ERROR')}
      className={getSuccessBadgeClass(success)}
    />
  );
}
