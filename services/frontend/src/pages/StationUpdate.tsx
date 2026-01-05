import {useAuth} from "../context/AuthContext.tsx";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import * as React from "react";
import {trackingService} from "../services/api.ts";
import {toast} from "sonner";
import {ErrorDiv} from "../components/ErrorDiv.tsx";

type UserRole = "SENDER" | "STATION" | "CUSTOMER" | "ADMIN" | null;

type StatusModes = "PICKED_UP" | "IN_TRANSIT" | "OUT_FOR_DELIVERY" | "DELIVERED" | "DELAYED";

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function StationUpdate() {
    const {role} = useAuth() as { user: any; role: UserRole };
    const navigate = useNavigate();

    const [shipmentId, setShipmentId] = useState("");
    const [status, setStatus] = useState<StatusModes>("PICKED_UP");
    const [message, setMessage] = useState("");
    const [lat, setLat] = useState("");
    const [lng, setLng] = useState("");

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const [geoLoading, setGeoLoading] = useState(false);

    const handleGetLocation = () => {
        if (!navigator.geolocation) {
            toast.error("Geolocation is not supported by your browser!");
            return;
        }
        setGeoLoading(true);
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                setLat(pos.coords.latitude.toFixed(6));
                setLng(pos.coords.longitude.toFixed(6));
                toast.success("Location filled in");
                setGeoLoading(false);
            },
            (err) => {
                toast.error(err.message || "Error retrieving your location.");
                setGeoLoading(false);
            },
            { enableHighAccuracy: true, timeout: 8000 }
        );
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const id = shipmentId.trim();
            if (!UUID_RE.test(id)) {
                setError("Shipment ID must be a valid UUID");
                return;
            }
            const latNum = lat.trim() === "" ? undefined : Number(lat);
            const longNum = lng.trim() === "" ? undefined : Number(lng);
            const onlyOneCoord =
                (latNum === undefined) !== (longNum === undefined);

            if (onlyOneCoord) {
                setError("Please provide both latitude and longitude, or leave both empty.");
                return;
            }

            if (latNum !== undefined && (latNum < -90 || latNum > 90)) {
                setError("Latitude must be between -90 and 90.");
                return;
            }
            if (longNum !== undefined && (longNum < -180 || longNum > 180)) {
                setError("Longitude must be between -180 and 180.");
                return;
            }

            const res = await trackingService.sendUpdate({
                shipmentId,
                status,
                message,
                lat: latNum,
                lng: longNum,
                timestamp: new Date().toISOString(),
            });
            console.log(res)
            toast.success("Update sent successfully");

            setShipmentId("");
            setStatus("IN_TRANSIT");
            setMessage("");
            setLat("");
            setLng("");
        } catch (error: any) {
            setError(error.message || "Error sending update");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (role === null) return;
        if (!(role === "STATION")) navigate("/dashboard");

    }, [role]);

    return (
        <div className="mx-auto w-full max-w-3xl space-y-6 px-4 sm:px-6 lg:px-8">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <button
                    onClick={() => navigate("/dashboard")}
                    className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-left text-sm font-medium text-white transition hover:bg-white/10 cursor-pointer"
                >
                    <span className="grid h-8 w-8 place-items-center rounded-lg bg-indigo-500/20 text-indigo-300">
                        &#8592;
                    </span>
                    Back to Dashboard
                </button>
                <div className="sm:text-right">
                    <h1 className="text-2xl font-semibold tracking-tight text-white">
                        Send An Update
                    </h1>
                    <p className="mt-1 text-sm text-white/60">
                        Update the status and location of a shipment.
                    </p>
                </div>
            </div>

            <div className="px-6 pt-7 pb-6 sm:px-8">
                {error && <ErrorDiv error={error}/>}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="grid grid-cols-1 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Shipment ID
                            </label>
                            <div className="mt-2">
                                <input
                                    type="text"
                                    value={shipmentId}
                                    onChange={(e) => setShipmentId(e.target.value)}
                                    disabled={loading}
                                    placeholder="394e2613-2b..."
                                    required
                                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                />
                            </div>
                        </div>
                    </div>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Status
                            </label>
                            <div className="mt-2">
                                <select
                                    value={status}
                                    onChange={(e) => setStatus(e.target.value as StatusModes)}
                                    disabled={loading}
                                    required
                                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                >
                                    <option value="PICKED_UP">PICKED UP</option>
                                    <option value="IN_TRANSIT">IN TRANSIT</option>
                                    <option value="OUT_FOR_DELIVERY">OUT FOR DELIVERY</option>
                                    <option value="DELIVERED">DELIVERED</option>
                                    <option value="DELAYED">DELAYED</option>
                                </select>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Message
                            </label>
                            <div className="mt-2">
                                <input
                                    type="text"
                                    value={message}
                                    onChange={(e) => setMessage(e.target.value)}
                                    disabled={loading}
                                    placeholder="e.g. Shipment was delivered"
                                    required
                                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                />
                            </div>
                        </div>
                    </div>
                    {(status === "IN_TRANSIT" || status === "OUT_FOR_DELIVERY") && (
                        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                            <div>
                                <label className="block text-sm font-medium text-white/80">
                                    Latitude
                                </label>
                                <div className="mt-2">
                                    <input
                                        type="number"
                                        inputMode="decimal"
                                        step="any"
                                        value={lat}
                                        onChange={(e) => setLat(e.target.value)}
                                        disabled={loading}
                                        placeholder="12.345"
                                        required={lng !== ""}
                                        className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-white/80">
                                    Longitude
                                </label>
                                <div className="mt-2">
                                    <input
                                        type="number"
                                        inputMode="decimal"
                                        step="any"
                                        value={lng}
                                        onChange={(e) => setLng(e.target.value)}
                                        disabled={loading}
                                        placeholder="1.234"
                                        required={lat !== ""}
                                        className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-white/80">
                                    Autofill Location
                                </label>
                                <div className="mt-2">
                                    <button
                                        type="button"
                                        onClick={handleGetLocation}
                                        disabled={geoLoading || loading}
                                        className="w-full rounded-xl border border-white/10 bg-white/10 px-4 py-3 text-white outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)] cursor-pointer"
                                    >
                                        {geoLoading ? "Retriving" : "Get Location"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="pt-2">
                        <button
                            type="submit"
                            disabled={loading}
                            className="group w-full rounded-xl bg-indigo-500 px-4 py-3 font-medium text-white shadow-lg shadow-indigo-500/20 transition hover:bg-indigo-400 disabled:opacity-60 disabled:hover:bg-indigo-500 cursor-pointer"
                        >
                                <span className="flex items-center justify-center gap-2">
                                    {loading && (
                                        <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white"/>
                                    )}
                                    {loading ? "Sending Update..." : "Send Update"}
                                </span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}

export default StationUpdate