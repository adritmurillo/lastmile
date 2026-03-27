-- V19: Update status values for automatic route closing feature
-- Migrate RETURNED stops to SKIPPED
-- Migrate CANCELLED routes to CLOSED
--
-- Note: Status columns use VARCHAR, not PostgreSQL ENUM types.
-- New status values (SKIPPED for orders, CLOSED for routes) don't require 
-- schema changes - they're just new string values inserted by the application.

-- 1. Migrate any existing RETURNED stops to SKIPPED
UPDATE stops SET status = 'SKIPPED' WHERE status = 'RETURNED';

-- 2. Migrate any existing CANCELLED routes to CLOSED
UPDATE routes SET status = 'CLOSED' WHERE status = 'CANCELLED';
