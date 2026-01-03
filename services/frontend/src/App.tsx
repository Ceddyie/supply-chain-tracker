import './App.css'
import {AuthProvider} from "./context/AuthContext.tsx";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import Login from "./pages/Login.tsx";
import Register from "./pages/Register.tsx";
import PrivateRoute from "./components/PrivateRoute.tsx";
import Layout from "./components/Layout.tsx";
import Dashboard from "./pages/Dashboard.tsx";
import CreateShipment from "./pages/CreateShipment.tsx";
import {Toaster} from "sonner";
import TrackingView from "./pages/TrackingView.tsx";
import StationUpdate from "./pages/StationUpdate.tsx";

function App() {
  return (
    <AuthProvider>
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route element={<Layout />}>
                    <Route path="track" element={<TrackingView />} />
                    <Route path="/track/:trackingId" element={<TrackingView />} />
                </Route>

                <Route element={<PrivateRoute/>}>
                    <Route element={<Layout />}>
                        <Route path="/" element={<Navigate to="/dashboard" replace />}/>
                        <Route path="dashboard" element={<Dashboard />} />
                        <Route path="shipments/new" element={<CreateShipment />} />
                        {/*<Route path="shipments" element={<MyShipments />} />*/}
                        <Route path="update" element={<StationUpdate />} />
                    </Route>

                </Route>

            </Routes>
        </BrowserRouter>

        <Toaster richColors position="top-right" />
    </AuthProvider>
  );
}

export default App
