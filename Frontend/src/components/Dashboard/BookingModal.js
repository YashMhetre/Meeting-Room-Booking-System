import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';

export const BookingModal = ({ show, onHide, room, hour, onBook, selectedDate }) => {
  const [duration, setDuration] = useState(60);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { user } = useAuth(); // ✅ Use useAuth hook instead of useContext

  // Reset state when modal opens/closes
  useEffect(() => {
    if (show) {
      setDuration(60);
      setError('');
      setLoading(false);
    }
  }, [show]);

  const handleBook = async () => {
    if (!room || !hour) {
      setError('Invalid booking details');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await onBook(room.id, `${hour}:00`, duration);
      // Success - modal will be hidden by parent
    } catch (err) {
      setError(err.message || 'Failed to book the room');
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setError('');
      onHide();
    }
  };

  // Prevent background clicks when modal is open
  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget && !loading) {
      handleClose();
    }
  };

  if (!show) return null;

  return (
    <div 
      className="modal show d-block" 
      style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1050 }}
      onClick={handleBackdropClick}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <h5 className="modal-title">Book Meeting Room</h5>
            <button 
              type="button" 
              className="btn-close" 
              onClick={handleClose}
              disabled={loading}
              aria-label="Close"
            ></button>
          </div>
          <div className="modal-body">
            {error && (
              <div className="alert alert-danger alert-dismissible fade show" role="alert">
                {error}
                <button 
                  type="button" 
                  className="btn-close" 
                  onClick={() => setError('')}
                  aria-label="Close"
                ></button>
              </div>
            )}

            <div className="mb-3">
              <label className="form-label fw-bold">Room</label>
              <p className="form-control-plaintext">{room?.name || 'N/A'}</p>
            </div>
            <div className="mb-3">
              <label className="form-label fw-bold">Date</label>
              <p className="form-control-plaintext">{selectedDate}</p>
            </div>
            <div className="mb-3">
              <label className="form-label fw-bold">Start Time</label>
              <p className="form-control-plaintext">{hour}:00</p>
            </div>
            <div className="mb-3">
              <label className="form-label fw-bold">Booked By</label>
              <p className="form-control-plaintext">
                {user?.preferred_username || user?.email || 'Unknown'}
              </p>
            </div>
            <div className="mb-3">
              <label className="form-label fw-bold">Duration</label>
              <select
                className="form-select"
                value={duration}
                onChange={(e) => setDuration(parseInt(e.target.value))}
                disabled={loading}
              >
                <option value={30}>30 minutes</option>
                <option value={45}>45 minutes</option>
                <option value={60}>60 minutes</option>
                <option value={90}>90 minutes</option>
                <option value={120}>120 minutes</option>
              </select>
            </div>
          </div>
          <div className="modal-footer">
            <button 
              className="btn btn-secondary" 
              onClick={handleClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button 
              className="btn btn-primary" 
              onClick={handleBook} 
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Booking...
                </>
              ) : (
                'Confirm Booking'
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};