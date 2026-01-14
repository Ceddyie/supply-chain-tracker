import axios from "axios";
import { auth } from "../config/firebase.ts";

const rawBase = import.meta.env.VITE_API_BASE_URL;
const API_BASE =
    rawBase && rawBase.length > 0
        ? `${rawBase}/api`
        : "http://localhost:8080/api";

const api = axios.create({ baseURL: API_BASE });

api.interceptors.request.use(async (config) => {
    const user = auth.currentUser;
    if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const shipmentService = {
    create: (data: { sender: string; receiver: string; receiverStreet: string, receiverCity: string; expectedDelivery: string }) =>
        api.post('/shipment/create', data),

    getById: (id: string) =>
        api.get(`/shipment/${id}`),

    getMyShipments: () =>
        api.get(`/shipment`),

    trackPublic: (trackingId: string) =>
        api.get(`/shipment/track/${trackingId}`),
};

export const trackingService = {
    sendUpdate: (data: {
        shipmentId: string;
        status: string;
        message: string;
        lat: number | undefined;
        lng: number | undefined;
        timestamp: string;
    }) => api.post('/tracking/update', data),
};

export default api;

