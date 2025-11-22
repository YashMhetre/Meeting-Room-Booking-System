import React, { useState, useEffect, useCallback } from 'react';
import { User, Calendar, LogOut } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { graphqlRequest } from '../../api/graphql';
import { TimelineGrid } from './TimelineGrid';
import { BookingModal } from './BookingModal';
import { MyBookings } from './MyBookings';

const Dashboard = () => {
  const { user, token, logout } = useAuth();
  const [rooms, setRooms] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [myBookings, setMyBookings] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [showModal, setShowModal] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [selectedHour, setSelectedHour] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = useCallback(async () => {
    try {
      setError('');
      
      const roomsQuery = `query { getAllRooms { id name capacity available } }`;
      const roomsData = await graphqlRequest(roomsQuery);
      setRooms(roomsData.getAllRooms.filter(r => r.available));

      const availabilityQuery = `
        query($date: String!) {
          getAllRoomAvailabilities(date: $date) {
            roomId
            roomName
            capacity
            bookedSlots {
              startTime
              endTime
              bookedBy
              bookingId
            }
          }
        }
      `;
      const availData = await graphqlRequest(availabilityQuery, { date: selectedDate });
      
      // Get current user info for comparison
      const currentUserName = user?.preferred_username || user?.email || '';
      
      // Transform the data to match the expected format
      const allBookings = availData.getAllRoomAvailabilities.flatMap(avail =>
        avail.bookedSlots.map(slot => {
          // Calculate duration from start and end time
          const [startHour, startMin] = slot.startTime.split(':').map(Number);
          const [endHour, endMin] = slot.endTime.split(':').map(Number);
          const durationMinutes = (endHour * 60 + endMin) - (startHour * 60 + startMin);
          
          return {
            id: slot.bookingId,
            startTime: slot.startTime,
            endTime: slot.endTime,
            durationMinutes: durationMinutes,
            userName: slot.bookedBy,
            userEmail: slot.bookedBy, // Use bookedBy as identifier since email not available
            status: 'ACTIVE',
            room: {
              id: avail.roomId,
              name: avail.roomName
            }
          };
        })
      );
      setBookings(allBookings);

      const myBookingsQuery = `query { getMyBookings { id bookingDate startTime endTime durationMinutes status room { id name } userName userEmail } }`;
      const myData = await graphqlRequest(myBookingsQuery, {}, token);
      setMyBookings(myData.getMyBookings);
    } catch (err) {
      console.error('Error loading data:', err);
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [selectedDate, token]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleSlotClick = (room, hour) => {
    console.log('Slot clicked:', room.name, hour);
    setSelectedRoom(room);
    setSelectedHour(hour);
    setShowModal(true);
  };

  const handleBook = async (roomId, startTime, duration) => {
    console.log('Booking - Room ID:', roomId, 'Type:', typeof roomId);
    console.log('Start Time:', startTime);
    console.log('Duration:', duration);
    console.log('Token:', token ? 'Present' : 'Missing');
    
    if (!token) {
      throw new Error('You must be logged in to book a room');
    }
    
    // Ensure roomId is a string for GraphQL ID type
    const roomIdString = String(roomId);
    
    const mutation = `
      mutation($input: BookingInput!) {
        createBooking(input: $input) {
          id
          bookingDate
          startTime
          endTime
          durationMinutes
          status
        }
      }
    `;
    
    try {
      const result = await graphqlRequest(
        mutation,
        { 
          input: { 
            roomId: roomIdString,
            bookingDate: selectedDate, 
            startTime, 
            durationMinutes: duration 
          } 
        },
        token
      );
      
      console.log('Booking successful:', result);
      setShowModal(false);
      await loadData();
    } catch (err) {
      console.error('Booking error:', err);
      throw err; // Re-throw to let modal handle it
    }
  };

  const handleComplete = async (id) => {
    try {
      // FIXED: Changed from Long to ID
      const mutation = `mutation($id: ID!) { completeBooking(id: $id) { id status } }`;
      await graphqlRequest(mutation, { id: String(id) }, token);
      await loadData();
    } catch (err) {
      console.error('Complete error:', err);
      alert('Failed to complete booking: ' + err.message);
    }
  };

  const handleCancel = async (id) => {
    try {
      // FIXED: Changed from Long to ID
      const mutation = `mutation($id: ID!) { cancelBooking(id: $id) { id status } }`;
      await graphqlRequest(mutation, { id: String(id) }, token);
      await loadData();
    } catch (err) {
      console.error('Cancel error:', err);
      alert('Failed to cancel booking: ' + err.message);
    }
  };

  const handleModalClose = () => {
    console.log('Modal closing');
    setShowModal(false);
    setSelectedRoom(null);
    setSelectedHour(null);
  };

  if (loading) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-vh-100 bg-light">
      <nav className="navbar navbar-dark bg-primary shadow">
        <div className="container-fluid">
          <span className="navbar-brand">
            <Calendar className="me-2" size={24} />
            Meeting Room Booking
          </span>
          <div className="d-flex align-items-center text-white">
            <User size={20} className="me-2" />
            <span className="me-3">{user?.name || user?.email || 'User'}</span>
            <button className="btn btn-outline-light btn-sm" onClick={logout}>
              <LogOut size={16} className="me-1" />
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="container-fluid py-4">
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

        <div className="row mb-4">
          <div className="col-md-6">
            <h2>Room Availability</h2>
            <p className="text-muted">Click on available slots (green +) to book</p>
          </div>
          <div className="col-md-6 text-md-end">
            <label className="me-2">Select Date:</label>
            <input
              type="date"
              className="form-control d-inline-block w-auto"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
            />
          </div>
        </div>

        <div className="row">
          <div className="col-lg-9 mb-4">
            <div className="card shadow">
              <div className="card-body p-2">
                <TimelineGrid 
                  rooms={rooms} 
                  bookings={bookings} 
                  onSlotClick={handleSlotClick}
                  currentUser={user}
                />
              </div>
            </div>
          </div>
          <div className="col-lg-3 mb-4">
            <MyBookings 
              bookings={myBookings} 
              onComplete={handleComplete} 
              onCancel={handleCancel} 
            />
          </div>
        </div>
      </div>

      <BookingModal
        show={showModal}
        onHide={handleModalClose}
        room={selectedRoom}
        hour={selectedHour}
        onBook={handleBook}
        selectedDate={selectedDate}
      />
    </div>
  );
};

export default Dashboard;