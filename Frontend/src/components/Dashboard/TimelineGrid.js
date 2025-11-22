import React from 'react';
import { Plus, Clock } from 'lucide-react';

export const TimelineGrid = ({ rooms, bookings, onSlotClick, currentUser }) => {
  
  // Check if user is admin by checking roles OR email
  const isAdmin = currentUser?.realm_access?.roles?.includes('admin') || 
                  currentUser?.resource_access?.['meeting-room-app']?.roles?.includes('admin') ||
                  currentUser?.realm_access?.roles?.includes('ADMIN') ||
                  currentUser?.email === 'admin@meetingroom.com' ||
                  currentUser?.preferred_username === 'admin@meetingroom.com' ||
                  false;
  
  // Debug log to check admin status
  console.log('Admin check:', { 
    isAdmin, 
    userEmail: currentUser?.email,
    preferredUsername: currentUser?.preferred_username,
    realmRoles: currentUser?.realm_access?.roles,
    resourceRoles: currentUser?.resource_access,
    currentUser 
  });
  // Get display name for booking
  const getBookingDisplayName = (booking) => {
    if (isAdmin) {
      return booking.userName;
    } else {
      // Get all possible user identifiers
      const currentUserEmail = currentUser?.email || '';
      const currentUserName = currentUser?.preferred_username || '';
      const currentUserGivenName = currentUser?.given_name || '';
      const currentUserFamilyName = currentUser?.family_name || '';
      const currentUserFullName = currentUser?.name || '';
      
      const bookingName = booking.userName || '';
      
      // Debug log to see what values we're comparing
      console.log('Comparing:', { 
        currentUserEmail, 
        currentUserName, 
        currentUserFullName,
        bookingName, 
        allCurrentUserData: currentUser 
      });
      
      // Check if it's the current user's booking (multiple checks)
      const isCurrentUser = 
        bookingName.toLowerCase() === currentUserEmail.toLowerCase() ||
        bookingName.toLowerCase() === currentUserName.toLowerCase() ||
        bookingName.toLowerCase() === currentUserFullName.toLowerCase() ||
        bookingName.toLowerCase() === `${currentUserGivenName} ${currentUserFamilyName}`.toLowerCase();
      
      return isCurrentUser ? 'Booked by me' : 'Booked by other';
    }
  };

  // Create 15-minute intervals from 9 AM to 6 PM (last slot starts at 17:45)
  const generateTimeSlots = () => {
    const slots = [];
    for (let hour = 9; hour < 18; hour++) {
      for (let minute = 0; minute < 60; minute += 15) {
        slots.push({
          hour,
          minute,
          label: `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
        });
      }
    }
    return slots;
  };

  const timeSlots = generateTimeSlots();

  // Convert time string to minutes since midnight
  const timeToMinutes = (timeStr) => {
    const [hour, minute] = timeStr.split(':').map(Number);
    return hour * 60 + minute;
  };

  // Check if a time slot is within a booking
  const getBookingAtSlot = (roomId, slotMinutes) => {
    return bookings.find(booking => {
      if (booking.room.id !== roomId) return false;
      
      const startMinutes = timeToMinutes(booking.startTime);
      const endMinutes = startMinutes + booking.durationMinutes;
      
      return slotMinutes >= startMinutes && slotMinutes < endMinutes;
    });
  };

  // Check if this slot is the start of a booking
  const isBookingStart = (roomId, slotMinutes) => {
    return bookings.find(booking => {
      if (booking.room.id !== roomId) return false;
      const startMinutes = timeToMinutes(booking.startTime);
      return slotMinutes === startMinutes;
    });
  };

  // Calculate how many 15-min slots a booking spans
  const getBookingSpan = (durationMinutes) => {
    return Math.ceil(durationMinutes / 15);
  };

  const handleCellClick = (room, slot) => {
    const booking = getBookingAtSlot(room.id, timeToMinutes(slot.label));
    if (!booking) {
      onSlotClick(room, slot.hour);
    }
  };

  return (
    <div className="timeline-container" style={{ overflowX: 'auto', width: '100%' }}>
      <style>{`
        .timeline-grid {
          display: grid;
          grid-template-columns: 180px repeat(${timeSlots.length}, minmax(40px, 1fr));
          gap: 0;
          background: #e9ecef;
          border: 1px solid #dee2e6;
          width: 100%;
          min-width: fit-content;
        }
        
        .timeline-header-cell,
        .timeline-cell,
        .timeline-room-header {
          background: white;
          padding: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          min-height: 60px;
          border-right: 1px solid #e9ecef;
          border-bottom: 1px solid #e9ecef;
        }
        
        .timeline-header-cell {
          font-size: 11px;
          font-weight: 600;
          color: #495057;
          writing-mode: horizontal-tb;
          position: sticky;
          top: 0;
          background: #f8f9fa;
          z-index: 10;
        }
        
        .timeline-room-header {
          position: sticky;
          left: 0;
          background: #f8f9fa;
          z-index: 11;
          justify-content: flex-start;
          font-weight: 600;
          border-right: 2px solid #dee2e6;
        }
        
        .timeline-cell {
          position: relative;
          cursor: pointer;
          transition: background-color 0.2s;
        }
        
        .timeline-cell.available {
          background-color: #5bd18eff;
        }
        
        .timeline-cell.available:hover {
          background-color: #a8e6c1;
        }
        
        .timeline-cell.booked {
          background-color: #ee8989ff;
          cursor: default;
        }
        
        .booking-block {
          position: absolute;
          left: 0;
          top: 0;
          height: 100%;
          background: transparent;
          color: #721c24;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          font-size: 11px;
          font-weight: 600;
          z-index: 5;
          padding: 4px;
          overflow: hidden;
          pointer-events: none;
          border: 2px solid #ec929bff;
        }
        
        .booking-block-content {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 2px;
          text-align: center;
        }
        
        .hour-marker {
          border-left: 2px solid #dee2e6;
        }
        
        .capacity-badge {
          font-size: 11px;
          color: #6c757d;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      `}</style>
      
      <div className="timeline-grid">
        {/* Header row */}
        <div className="timeline-header-cell" style={{ position: 'sticky', left: 0, zIndex: 12 }}>
          Room / Time
        </div>
        {timeSlots.map((slot, idx) => (
          <div 
            key={`header-${idx}`} 
            className={`timeline-header-cell ${slot.minute === 0 ? 'hour-marker' : ''}`}
          >
            {slot.minute === 0 ? `${slot.hour}:00` : ''}
          </div>
        ))}
        
        {/* Room rows */}
        {rooms.map(room => (
          <React.Fragment key={room.id}>
            {/* Room name cell */}
            <div className="timeline-room-header">
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600 }}>{room.name}</div>
              </div>
            </div>
            
            {/* Time slot cells */}
            {timeSlots.map((slot, idx) => {
              const slotMinutes = timeToMinutes(slot.label);
              const booking = getBookingAtSlot(room.id, slotMinutes);
              const bookingStart = isBookingStart(room.id, slotMinutes);
              
              return (
                <div
                  key={`${room.id}-${idx}`}
                  className={`timeline-cell ${booking ? 'booked' : 'available'} ${slot.minute === 0 ? 'hour-marker' : ''}`}
                  onClick={() => handleCellClick(room, slot)}
                  title={booking ? `${getBookingDisplayName(booking)} - ${booking.durationMinutes} min` : 'Click to book'}
                >
                  {bookingStart && (
                    <div 
                      className="booking-block"
                      style={{ 
                        width: `calc(${getBookingSpan(bookingStart.durationMinutes) * 100}% + ${(getBookingSpan(bookingStart.durationMinutes) - 1) * 1}px)`
                      }}
                    >
                      <div className="booking-block-content">
                        <div style={{ fontSize: '10px', fontWeight: 'bold' }}>
                          {getBookingDisplayName(bookingStart)}
                        </div>
                        <div style={{ fontSize: '9px', display: 'flex', alignItems: 'center', gap: 2 }}>
                          <Clock size={10} />
                          {bookingStart.durationMinutes}m
                        </div>
                      </div>
                    </div>
                  )}
                  {!booking && (
                    <Plus size={14} style={{ color: '#adb5bd', opacity: 0.5 }} />
                  )}
                </div>
              );
            })}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
};