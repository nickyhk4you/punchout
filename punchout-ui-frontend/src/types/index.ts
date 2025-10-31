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
