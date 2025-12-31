import axios from "axios";
import { auth } from "../config/firebase.ts";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

api.interceptors.request.use(async (config) => {
    const user = auth.currentUser;
    if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const shipmentService = {
    create: (data: { sender: string; receiver: string; expectedDelivery: string }) =>
        api.post('/shipments/create', data),

    getById: (id: string) =>
        api.get(`/shipments/${id}`),

    getMyShipments: () =>
        api.get(`/shipments`),

    trackPublic: (trackingId: string) =>
        api.get(`/shipments/track/${trackingId}`),
};

export const trackingService = {
    sendUpdate: (data: {
        shipmentId: string;
        status: string;
        message: string;
        lat: number;
        lng: number;
        timestamp: string;
    }) => api.post('/tracking/update', data),
};

export default api;

