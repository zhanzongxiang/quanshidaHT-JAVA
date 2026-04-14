# Waybill Module V1

## Goal

Build an initial waybill management module for admin input and customer tracking.

The first version covers:

- Admin can create and edit waybills
- Admin can maintain route legs for transfer and re-waybill scenarios
- Admin can maintain customer-visible tracking events
- Public tracking endpoint can query real waybill data

## Business Flow

1. Customer ships cargo to company warehouse
2. Warehouse receives and registers the waybill
3. Company arranges outbound shipment
4. If transfer or re-waybill is needed, new route legs are added
5. Tracking events are recorded across the full journey
6. Customer queries by master tracking number or sub tracking number

## Core Concepts

### Master Waybill

The main business record.

Suggested fields:

- `mainTrackingNo`: public-facing primary tracking number
- `referenceNo`: optional internal or customer reference number
- `customerName`
- `customerPhone`
- `originWarehouse`
- `destinationCountry`
- `destinationCity`
- `routeType`: `direct` or `transfer`
- `currentStatus`
- `currentNode`
- `cargoDescription`
- `packageCount`
- `weightKg`
- `remark`

### Route Leg

Represents one segment in the shipment route.

Examples:

- customer -> warehouse
- warehouse -> transfer hub
- transfer hub -> destination country
- destination country -> local delivery

Suggested fields:

- `legNo`
- `legType`
- `carrierName`
- `trackingNo`
- `fromNode`
- `toNode`
- `legStatus`
- `transferFlag`
- `departureTime`
- `arrivalTime`
- `remark`

### Tracking Event

Customer-visible timeline entry.

Suggested fields:

- `eventTime`
- `eventStatus`
- `eventDescription`
- `eventLocation`
- `visibleToCustomer`
- `sortNo`
- `legId` optional

## Status Suggestion

Master waybill status:

- `created`
- `received`
- `processing`
- `in_transit`
- `transferred`
- `customs_clearance`
- `delivering`
- `signed`
- `exception`

Leg status:

- `pending`
- `received`
- `departed`
- `arrived`
- `completed`
- `exception`

## API Draft

Admin APIs:

- `GET /api/waybills`
- `GET /api/waybills/{id}`
- `POST /api/waybills`
- `PUT /api/waybills/{id}`
- `DELETE /api/waybills/{id}`

Public API:

- `GET /api/tracking/{trackingNo}`

Tracking query should match:

- master tracking number
- route leg tracking number

## Initial UI Scope

Admin page includes:

- list with keyword and status filter
- create or edit dialog
- base form for master waybill
- editable route leg list
- editable tracking event list

## Out of Scope For V1

- automatic carrier callbacks
- batch import
- barcode printing
- billing and settlement
- customer account binding
- attachments and customs document upload

## Notes

This version is intended to fix the module boundary first.

Implementation choices in this repository:

- one master table for waybill header
- one child table for route legs
- one child table for tracking events
- public tracking falls back to mock data when no real waybill is found
