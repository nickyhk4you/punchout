import { OrderObject } from '@/types';

interface OrderObjectCardProps {
  orderObject: OrderObject | null;
}

export default function OrderObjectCard({ orderObject }: OrderObjectCardProps) {
  return (
    <div className="card shadow-sm mb-4">
      <div className="card-body">
        <h5 className="card-title d-flex align-items-center mb-4">
          <i className="fas fa-shopping-cart me-2 text-success"></i>
          Order Object
        </h5>
        {orderObject ? (
          <div className="row g-3">
            <InfoItem label="Type" value={orderObject.type} />
            <InfoItem label="Operation" value={orderObject.operation} />
            <InfoItem label="Mode" value={orderObject.mode} />
            <InfoItem label="User Email" value={orderObject.userEmail} />
            <InfoItem label="Company Code" value={orderObject.companyCode} />
            <InfoItem 
              label="User Name" 
              value={
                orderObject.userFirstName && orderObject.userLastName
                  ? `${orderObject.userFirstName} ${orderObject.userLastName}`
                  : undefined
              } 
            />
            <InfoItem label="From Identity" value={orderObject.fromIdentity} />
            <InfoItem label="Sold To Lookup" value={orderObject.soldToLookup} />
          </div>
        ) : (
          <div className="text-center py-5">
            <i className="fas fa-inbox text-muted fs-1 mb-2 d-block"></i>
            <p className="text-muted">No order object found for this session</p>
          </div>
        )}
      </div>
    </div>
  );
}

interface InfoItemProps {
  label: string;
  value?: string;
}

function InfoItem({ label, value }: InfoItemProps) {
  return (
    <div className="col-md-4">
      <small className="text-muted d-block">{label}</small>
      <div>{value || '-'}</div>
    </div>
  );
}
