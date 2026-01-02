import './App.css'
import {AuthProvider} from "./context/AuthContext.tsx";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import Login from "./pages/Login.tsx";
import Register from "./pages/Register.tsx";
import PrivateRoute from "./components/PrivateRoute.tsx";
import Layout from "./components/Layout.tsx";
import Dashboard from "./pages/Dashboard.tsx";

function App() {
  return (
    <AuthProvider>
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                {/*<Route path="/track/:trackingId" element={<TrackingView />} />
                <Route path="/track" element={<TrackingView />} />*/}

                <Route path="/" element={<PrivateRoute><Layout children={<Dashboard />}/></PrivateRoute>}>
                    <Route index element={<Navigate to="/dashboard" />} />
                    <Route path="dashboard" element={<Dashboard />} />
                    {/*<Route path="shipments/new" element={<CreateShipment />} />
                    <Route path="shipments" element={<MyShipments />} />
                    <Route path="station" element={<StationUpdate />} />*/}
                </Route>

            </Routes>
        </BrowserRouter>
    </AuthProvider>
  );
}

export default App
