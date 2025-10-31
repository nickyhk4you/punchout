export interface PunchoutSession {
  sessionKey: string;
  cartReturn: string;
  operation: string;
  contact: string;
  routeName: string;
  environment: string;
  flags: string;
  sessionDate: string;
}

export interface PunchoutSessionDetail extends PunchoutSession {
  punchedOutDate: string;
  catalog: string;
  network: string;
  parser: string;
  buyerCookie: string;
}
