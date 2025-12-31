import './App.css'
import {AuthProvider /*, useAuth*/} from "./context/AuthContext.tsx";
//import * as React from "react";
import {BrowserRouter, /*Navigate,*/ Route, Routes} from "react-router-dom";
import Login from "./pages/Login.tsx";
import Register from "./pages/Register.tsx";

/*function PrivateRoute({ children }: { children: React.ReactNode }) {
    const { user, loading } = useAuth();

    if (loading) {
        return <div className="min-h-screen flex items-center justify-center">Loading...</div>
    }

    return user ? <>{children}</> : <Navigate to="/login" />;
}*/

function App() {
  return (
    <AuthProvider>
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />

                {/*<Route path="/" element={<PrivateRoute></PrivateRoute>}*/}
            </Routes>
        </BrowserRouter>
    </AuthProvider>
  );
}

export default App
