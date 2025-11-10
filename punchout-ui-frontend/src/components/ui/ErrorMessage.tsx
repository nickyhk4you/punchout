import Link from 'next/link';

interface ErrorMessageProps {
  message: string;
  backLink?: string;
  backLabel?: string;
}

export default function ErrorMessage({ 
  message, 
  backLink = '/sessions', 
  backLabel = 'Back to Sessions' 
}: ErrorMessageProps) {
  return (
    <div className="container py-5">
      <div className="text-center py-5">
        <div className="mb-4">
          <i className="fas fa-exclamation-circle text-danger" style={{fontSize: '4rem'}}></i>
        </div>
        <h4 className="text-danger mb-4">{message}</h4>
        <Link href={backLink} className="btn btn-primary">
          <i className="fas fa-arrow-left me-2"></i>
          {backLabel}
        </Link>
      </div>
    </div>
  );
}
