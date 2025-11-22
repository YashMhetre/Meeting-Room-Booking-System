import { Calendar, Clock, CheckCircle, XCircle, MapPin } from 'lucide-react';

export const MyBookings = ({ bookings, onComplete, onCancel }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'primary';
      case 'COMPLETED': return 'success';
      case 'CANCELLED': return 'danger';
      default: return 'secondary';
    }
  };

  const getStatusBadge = (status) => {
    const color = getStatusColor(status);
    return `badge bg-${color}`;
  };

  return (
    <div className="card shadow h-100">
      <div className="card-header bg-primary text-white">
        <h5 className="mb-0">
          <Calendar size={20} className="me-2" />
          My Bookings
        </h5>
      </div>
      
      <div className="card-body p-0" style={{ maxHeight: '600px', overflowY: 'auto' }}>
        {bookings.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <Calendar size={48} className="mb-3 opacity-50" />
            <p>No bookings found</p>
          </div>
        ) : (
          <div className="list-group list-group-flush">
            {bookings.map(b => (
              <div key={b.id} className="list-group-item">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <div className="flex-grow-1">
                    <h6 className="mb-1 d-flex align-items-center">
                      <MapPin size={16} className="me-1 text-primary" />
                      {b.room.name}
                    </h6>
                    <div className="small text-muted mb-1">
                      <Calendar size={14} className="me-1" />
                      {b.bookingDate}
                    </div>
                    <div className="small text-muted">
                      <Clock size={14} className="me-1" />
                      {b.startTime} ({b.durationMinutes} min)
                    </div>
                  </div>
                  <span className={getStatusBadge(b.status)}>
                    {b.status}
                  </span>
                </div>
                
                {b.status === 'ACTIVE' && (
                  <div className="btn-group w-100 mt-2" role="group">
                    <button 
                      className="btn btn-sm btn-success"
                      onClick={() => onComplete(b.id)}
                      title="Mark as completed"
                    >
                      <CheckCircle size={14} className="me-1" />
                      Complete
                    </button>
                    <button 
                      className="btn btn-sm btn-danger"
                      onClick={() => onCancel(b.id)}
                      title="Cancel booking"
                    >
                      <XCircle size={14} className="me-1" />
                      Cancel
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};