const GRAPHQL_ENDPOINT = 'http://localhost:8080/graphql';

let isRefreshing = false;
let refreshSubscribers = [];

const subscribeTokenRefresh = (cb) => {
  refreshSubscribers.push(cb);
};

const onTokenRefreshed = (token) => {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
};

const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refresh_token');
  
  if (!refreshToken) {
    throw new Error('No refresh token available');
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
      throw new Error('Token refresh failed');
    }

    const data = await response.json();
    
    localStorage.setItem('access_token', data.access_token);
    localStorage.setItem('refresh_token', data.refresh_token);
    
    return data.access_token;
  } catch (error) {
    // Clear tokens and redirect to login
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    window.location.href = '/';
    throw error;
  }
};

export const graphqlRequest = async (query, variables = {}) => {
  // Get token from localStorage
  let token = localStorage.getItem('access_token');
  
  const headers = { 
    'Content-Type': 'application/json',
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  } else {
    console.warn('No token found in localStorage');
  }

  try {
    const response = await fetch(GRAPHQL_ENDPOINT, {
      method: 'POST',
      headers,
      body: JSON.stringify({ query, variables }),
    });

    // If we get 401, try to refresh the token
    if (response.status === 401) {
      console.log('Got 401, attempting to refresh token...');

      if (!isRefreshing) {
        isRefreshing = true;
        
        try {
          const newToken = await refreshAccessToken();
          isRefreshing = false;
          onTokenRefreshed(newToken);

          // Retry the original request with new token
          headers['Authorization'] = `Bearer ${newToken}`;
          
          const retryResponse = await fetch(GRAPHQL_ENDPOINT, {
            method: 'POST',
            headers,
            body: JSON.stringify({ query, variables }),
          });

          if (!retryResponse.ok) {
            throw new Error(`HTTP error! status: ${retryResponse.status}`);
          }

          const result = await retryResponse.json();
          
          if (result.errors) {
            console.error('GraphQL Errors:', result.errors);
            throw new Error(result.errors[0].message);
          }
          
          return result.data;
        } catch (refreshError) {
          isRefreshing = false;
          throw refreshError;
        }
      } else {
        // Wait for token refresh to complete
        return new Promise((resolve, reject) => {
          subscribeTokenRefresh(async (newToken) => {
            headers['Authorization'] = `Bearer ${newToken}`;
            
            try {
              const retryResponse = await fetch(GRAPHQL_ENDPOINT, {
                method: 'POST',
                headers,
                body: JSON.stringify({ query, variables }),
              });

              const result = await retryResponse.json();
              
              if (result.errors) {
                reject(new Error(result.errors[0].message));
              } else {
                resolve(result.data);
              }
            } catch (error) {
              reject(error);
            }
          });
        });
      }
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    
    if (result.errors) {
      console.error('GraphQL Errors:', result.errors);
      throw new Error(result.errors[0].message);
    }
    
    return result.data;
  } catch (error) {
    console.error('GraphQL Request Error:', error);
    throw error;
  }
};