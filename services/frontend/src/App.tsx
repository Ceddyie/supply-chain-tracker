import './App.css'
import {AuthProvider} from "./context/AuthContext.tsx";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import Login from "./pages/Login.tsx";
import Register from "./pages/Register.tsx";
import PrivateRoute from "./components/PrivateRoute.tsx";
import Layout from "./components/Layout.tsx";
import Dashboard from "./pages/Dashboard.tsx";
import CreateShipment from "./pages/CreateShipment.tsx";

function App() {
  return (
    <AuthProvider>
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                {/*<Route path="/track/:trackingId" element={<TrackingView />} />
                <Route path="/track" element={<TrackingView />} />*/}

                <Route element={<PrivateRoute/>}>
                    <Route element={<Layout />}>
                        <Route path="/" element={<Navigate to="/dashboard" replace />}/>
                        <Route path="dashboard" element={<Dashboard />} />
                        <Route path="shipments/new" element={<CreateShipment />} />
                        {/*<Route path="shipments" element={<MyShipments />} />
                        <Route path="station" element={<StationUpdate />} />*/}
                    </Route>

                </Route>

            </Routes>
        </BrowserRouter>
    </AuthProvider>
  );
}

export default App
