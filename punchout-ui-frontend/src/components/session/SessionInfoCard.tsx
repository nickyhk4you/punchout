import { PunchOutSession } from '@/types';

interface SessionInfoCardProps {
  session: PunchOutSession;
}

export default function SessionInfoCard({ session }: SessionInfoCardProps) {
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  };

  const formatCurrency = (value?: number) => {
    if (!value) return '-';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  return (
    <div className="card shadow-sm mb-4">
      <div className="card-body">
        <h5 className="card-title d-flex align-items-center mb-4">
          <i className="fas fa-info-circle me-2 text-primary"></i>
          Session Information
        </h5>
        <div className="row g-3">
          <InfoItem label="Session Key" value={session.sessionKey} />
          
          <InfoItem 
            label="Operation" 
            value={
              <span className={`badge ${
                session.operation === 'CREATE' ? 'bg-success' :
                session.operation === 'EDIT' ? 'bg-warning text-dark' :
                'bg-info'
              }`}>
                {session.operation}
              </span>
            }
          />
          
          <InfoItem 
            label="Environment" 
            value={
              <span className={`badge ${
                session.environment === 'PRODUCTION' ? 'bg-danger' :
                session.environment === 'STAGING' ? 'bg-warning text-dark' :
                'bg-secondary'
              }`}>
                {session.environment}
              </span>
            }
          />
          
          <InfoItem label="Contact Email" value={session.contactEmail} />
          <InfoItem label="Route Name" value={session.routeName} />
          <InfoItem label="Network" value={session.network} />
          <InfoItem label="Session Date" value={formatDate(session.sessionDate)} />
          <InfoItem label="Punched In" value={formatDate(session.punchedIn)} />
          <InfoItem label="Punched Out" value={formatDate(session.punchedOut)} />
          <InfoItem label="Order ID" value={session.orderId} />
          <InfoItem label="Order Value" value={formatCurrency(session.orderValue)} className="fw-bold" />
          <InfoItem label="Line Items" value={session.lineItems} />
          <InfoItem label="Item Quantity" value={session.itemQuantity} />
          <InfoItem label="Catalog" value={session.catalog} />
          <InfoItem label="Parser" value={session.parser} />
          
          <div className="col-md-12">
            <InfoItem label="Cart Return URL" value={session.cartReturn} className="text-break" />
          </div>
        </div>
      </div>
    </div>
  );
}

interface InfoItemProps {
  label: string;
  value?: string | number | React.ReactNode;
  className?: string;
}

function InfoItem({ label, value, className = '' }: InfoItemProps) {
  return (
    <div className="col-md-4">
      <small className="text-muted d-block">{label}</small>
      <div className={className}>{value || '-'}</div>
    </div>
  );
}
