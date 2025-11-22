import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null);
  const [refreshToken, setRefreshToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Check if token exists in localStorage on mount
    const storedToken = localStorage.getItem('access_token');
    const storedRefreshToken = localStorage.getItem('refresh_token');
    
    if (storedToken) {
      setToken(storedToken);
      setRefreshToken(storedRefreshToken);
      // Decode and set user info
      try {
        const payload = JSON.parse(atob(storedToken.split('.')[1]));
        setUser(payload);
      } catch (e) {
        console.error('Error decoding token:', e);
        // Token might be expired or invalid
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      console.log('Attempting login with:', email);
      
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      console.log('Login response status:', response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Login failed response:', errorText);
        
        let errorMessage = 'Login failed';
        try {
          const errorData = JSON.parse(errorText);
          errorMessage = errorData.message || errorData.error || 'Login failed';
        } catch (e) {
          errorMessage = errorText || 'Login failed';
        }
        
        throw new Error(errorMessage);
      }

      const data = await response.json();
      console.log('Login response data:', data);
      
      // Handle different possible response formats
      const accessToken = data.access_token || data.accessToken || data.token;
      const refreshTokenValue = data.refresh_token || data.refreshToken;
      
      if (!accessToken) {
        console.error('No access token in response:', data);
        throw new Error('No access token received from server');
      }

      // Store tokens
      localStorage.setItem('access_token', accessToken);
      if (refreshTokenValue) {
        localStorage.setItem('refresh_token', refreshTokenValue);
      }
      
      setToken(accessToken);
      setRefreshToken(refreshTokenValue);

      // Decode user info
      try {
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        console.log('Decoded token payload:', payload);
        setUser(payload);
      } catch (e) {
        console.error('Error decoding token:', e);
      }

      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, error: error.message };
    }
  };

  const logout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    setToken(null);
    setRefreshToken(null);
    setUser(null);
  };

  const refreshAccessToken = async () => {
    if (!refreshToken) {
      logout();
      return false;
    }

    try {
      const response = await fetch('http://localhost:8080/api/auth/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (!response.ok) {
        logout();
        return false;
      }

      const data = await response.json();
      
      const accessToken = data.access_token || data.accessToken || data.token;
      const refreshTokenValue = data.refresh_token || data.refreshToken;
      
      if (accessToken) {
        localStorage.setItem('access_token', accessToken);
        setToken(accessToken);
      }
      
      if (refreshTokenValue) {
        localStorage.setItem('refresh_token', refreshTokenValue);
        setRefreshToken(refreshTokenValue);
      }

      return true;
    } catch (error) {
      console.error('Token refresh error:', error);
      logout();
      return false;
    }
  };

  return (
    <AuthContext.Provider value={{ 
      token, 
      user, 
      login, 
      logout, 
      loading,
      refreshAccessToken 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};