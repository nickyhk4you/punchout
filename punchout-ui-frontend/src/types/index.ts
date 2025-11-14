export interface PunchOutSession {
  sessionKey: string;
  cartReturn?: string;
  operation: string;
  contactEmail?: string;
  routeName?: string;
  environment?: string;
  flags?: string;
  sessionDate: string;
  punchedIn?: string;
  punchedOut?: string;
  orderId?: string;
  orderValue?: number;
  lineItems?: number;
  itemQuantity?: number;
  catalog?: string;
  network?: string;
  parser?: string;
  buyerCookie?: string;
}

export interface OrderObject {
  sessionKey: string;
  type?: string;
  operation?: string;
  mode?: string;
  uniqueName?: string;
  userEmail?: string;
  companyCode?: string;
  userFirstName?: string;
  userLastName?: string;
  fromIdentity?: string;
  soldToLookup?: string;
  contactEmail?: string;
}

export interface GatewayRequest {
  id?: number;
  sessionKey: string;
  datetime: string;
  uri: string;
  openLink?: string;
}

export interface SessionFilter {
  operation?: string;
  routeName?: string;
  environment?: string;
  startDate?: string;
  endDate?: string;
}

export interface NetworkRequest {
  id: string;
  sessionKey: string;
  orderId?: string;
  invoiceNumber?: string;
  requestId: string;
  timestamp: string;
  direction: 'INBOUND' | 'OUTBOUND';
  source: string;
  destination: string;
  method: string;
  url: string;
  headers?: Record<string, string>;
  requestBody?: string;
  statusCode?: number;
  responseHeaders?: Record<string, string>;
  responseBody?: string;
  duration?: number;
  requestType: string;
  success: boolean;
  errorMessage?: string;
}

export interface Order {
  id: string;
  orderId: string;
  sessionKey?: string;
  orderDate: string;
  orderType: string;
  orderVersion: string;
  customerId: string;
  customerName: string;
  total: number;
  currency: string;
  taxAmount?: number;
  shipTo?: OrderAddress;
  billTo?: OrderAddress;
  items: OrderItem[];
  extrinsics?: Record<string, string>;
  comments?: string;
  status: string;
  receivedAt: string;
  processedAt?: string;
  muleOrderId?: string;
  environment?: string;
  source?: string;
  dialect?: string;
}

export interface OrderAddress {
  addressId?: string;
  name: string;
  deliverTo?: string;
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  email?: string;
  phone?: string;
}

export interface OrderItem {
  lineNumber: number;
  quantity: number;
  supplierPartId: string;
  supplierPartAuxiliaryId?: string;
  description: string;
  unitPrice: number;
  extendedAmount: number;
  currency: string;
  unitOfMeasure?: string;
  unspsc?: string;
  url?: string;
  extrinsics?: Record<string, string>;
}

export interface EnvironmentConfig {
  environment: string;
  url: string;
  username: string;
  password: string;
  sharedSecret: string;
  enabled: boolean;
  notes?: string;
}

export interface CatalogRoute {
  id: string;
  routeName: string;
  domain: string;
  network: string;
  type: string;
  description?: string;
  environments: EnvironmentConfig[];
  active: boolean;
  createdDate: string;
  lastModified: string;
}

export interface PunchOutTest {
  id: string;
  testName: string;
  catalogRouteId: string;
  catalogRouteName?: string;
  environment: string;
  tester: string;
  testDate: string;
  status: string;
  setupRequestSent?: string;
  setupResponseReceived?: string;
  catalogUrl?: string;
  orderMessageSent?: string;
  orderMessageReceived?: string;
  totalDuration?: number;
  setupRequest?: string;
  setupResponse?: string;
  orderMessage?: string;
  orderResponse?: string;
  errorMessage?: string;
  notes?: string;
  sessionKey?: string;
}

export interface CxmlTemplate {
  id?: string;
  templateName: string;
  environment: string;
  customerId: string;
  customerName: string;
  cxmlTemplate: string;
  description?: string;
  isDefault?: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
}

export interface Invoice {
  id: string;
  invoiceNumber: string;
  orderId?: string;
  sessionKey?: string;
  poNumber: string;
  routeName: string;
  environment: string;
  flags?: string;
  invoiceTotal: number;
  currency: string;
  receivedDate: string;
  invoiceDate: string;
  dueDate?: string;
  status: string;
  customerId: string;
  customerName: string;
  supplierName?: string;
  taxAmount?: number;
  shippingAmount?: number;
  subtotal?: number;
  shipTo?: any;
  billTo?: any;
  lineItems?: any[];
  paymentTerms?: string;
  notes?: string;
  processedAt?: string;
  paidAt?: string;
  source?: string;
}

export interface CustomerDatastore {
  id?: string;
  customer: string;
  environment: string;
  keyValuePairs: Record<string, string>;
  description?: string;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}
