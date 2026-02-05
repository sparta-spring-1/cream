import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoute = () => {
    // Check for token in localStorage (or your auth state management)
    // Assuming 'accessToken' is the key used for storing the JWT
    const isAuthenticated = !!localStorage.getItem('accessToken');

    if (!isAuthenticated) {
        // Redirect to login page if no token is found
        // replace prevents the user from going back to the protected page via back button
        return <Navigate to="/auth" replace />;
    }

    // Render the child routes if authenticated
    return <Outlet />;
};

export default ProtectedRoute;
