import {useNavigate, useParams} from "react-router-dom";
import {shipmentService} from "../services/api.ts";
import {useEffect, useState} from "react";
import * as React from "react";
import {TrackShipment} from "../components/TrackShipment.tsx";
import {CurrentLocationMap} from "../components/CurrentLocationMap.tsx";

type Shipment = {
    trackingId: string,
    status: string,
    expectedDelivery: Date,
    checkpoints: Checkpoint[]
};

type Checkpoint = {
    timestamp: Date,
    status: string,
    message: string,
    lat?: number,
    lng?: number
};

type ShipmentDto = {
    trackingId: string,
    status: string,
    expectedDelivery: string,
    checkpoints: Array<{
        timestamp: string,
        status: string,
        message: string,
        lat: number | null,
        lng: number | null,
    }>;
};

export function mapShipment(dto: ShipmentDto): Shipment {
    return {
        trackingId: dto.trackingId,
        status: dto.status,
        expectedDelivery: new Date(dto.expectedDelivery),
        checkpoints: dto.checkpoints.map((cp) => ({
            timestamp: new Date(cp.timestamp),
            status: cp.status,
            message: cp.message,
            lat: cp.lat ?? undefined,
            lng: cp.lng ?? undefined,
        })).sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime()),
    };
}

export default function TrackingView() {
    const {trackingId} = useParams<{trackingId?: string}>();
    const [newTrackingId, setNewTrackingId] = useState("");

    const navigate = useNavigate();

    const [shipment, setShipment] = useState<Shipment | null>(null);
    const checkpoints = shipment?.checkpoints ?? [];

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadTracking = async () => {
        if (!trackingId) return;

        setLoading(true);
        setError("");

        try {
            const res = await shipmentService.trackPublic(trackingId.toString());
            setShipment(mapShipment(res.data));
        } catch (error: any) {
            setError(error.message || "Something went wrong...");
            console.log("Failed to load shipment", error);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
      if (trackingId) loadTracking();
      else setLoading(false);
    }, [trackingId]);

    const handleTrack = (e: React.FormEvent) => {
        e.preventDefault();
        const id = newTrackingId.trim();
        if (id) navigate(`/track/${id}`);
    };

    const currentCpWithCoords = checkpoints.find((checkpoint) => checkpoint.lat !== undefined && checkpoint.lng !== undefined) ?? null;

    const TrackingDetails = () => (
        <div>
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-2">
                <div className={`rounded-xl border ${shipment?.status === "DELIVERED" ? "border-emerald-500/30 bg-emerald-500/10" : "border-white/10 bg-white/5"} p-2 text-center`}>
                    <p className="mt-1 text-sm text-white/60">Status</p>
                    <span className="text-xl">{shipment?.status.replaceAll("_", " ")}</span>
                </div>
                <div className="rounded-xl border border-white/10 bg-white/5 p-2 text-center">
                    <p className="mt-1 text-sm text-white/60">Expected Arrival</p>
                    <span className="text-xl">{shipment?.expectedDelivery.toLocaleDateString("de-DE")}</span>
                </div>
            </div>

            <h5 className="font-semibold tracking-tight text-white pt-6 pb-5">Timeline</h5>

            <div className="space-y-3">
                {checkpoints.map((cp, idx) => {
                    const isLatest = idx === 0;
                    const isLast = idx === checkpoints.length - 1;

                    return (
                        <div key={cp.timestamp.toISOString()} className="grid grid-cols-[28px_1fr] gap-3">
                            <div className="flex flex-col items-center">
                                <div
                                    className={[
                                        "mt-2 h-3.5 w-3.5 rounded-full border",
                                        isLatest
                                        ? "border-indigo-300 bg-indigo-400/40 shadow-[0_0_0_4px_rgba(99,102,241,0.18)]"
                                            : "border-white/20 bg-white/10",
                                    ].join(" ")}
                                />
                                {!isLast && <div className="mt-2 w-px flex-1 bg-white/10" />}
                            </div>

                            <div
                                className={[
                                    "rounded-2xl border bg-white/5 p-4",
                                    isLatest ? "border-indigo-400/30 bg-white/10" : "border-white/10",
                                ].join(" ")}
                            >
                                <div className="flex flex-wrap items-center justify-between gap-2">
                                    <div className="flex items-center gap-2">
                                        <span
                                            className={[
                                                "rounded-full border px-2.5 py-1 text-xs font-medium",
                                                isLatest
                                                ? "border-indigo-400/30 bg-indigo-500/10 text-indigo-200"
                                                :    "border-white/10 bg-white/5 text-white/80",
                                            ].join(" ")}
                                            >
                                            {cp.status.replaceAll("_", " ")}
                                        </span>

                                        {isLatest && (
                                            <span className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2.5 py-1 text-xs font-medium text-emerald-200">
                                                Latest
                                            </span>
                                        )}
                                    </div>

                                    <span className="text-xs text-white/60">{cp.timestamp.toLocaleString("de-DE")}</span>
                                </div>

                                <div className="mt-3 rounded-xl border border-white/10 bg-black/5 px-3 py-2">
                                    <p className="text-xs text-white/60">Message</p>
                                    <p className="mt-1 text-sm text-white">{cp.message}</p>
                                </div>

                                {(cp.lat !== undefined && cp.lng !== undefined) && (
                                    <div className="mt-3 text-xs text-white/60">
                                        Location: <span className="text-white/80">{cp.lat.toFixed(5)}, {cp.lng.toFixed(5)}</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    )
                })}
                {currentCpWithCoords && shipment?.status !== "DELIVERED" ? (
                    <CurrentLocationMap lat={currentCpWithCoords.lat!} lng={currentCpWithCoords.lng!} label={currentCpWithCoords.status.replaceAll("_", " ")} />
                ) : shipment?.status !== "DELIVERED" ? (
                    <div className="rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-white/60">
                        No location data available for this shipment
                    </div>
                ) :
                    <div className="rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-white/60">
                        Package has arrived its destination
                    </div>
                }
            </div>
        </div>
    )

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold tracking-tight text-white">
                        Tracking {trackingId ? trackingId : undefined}
                    </h1>
                    {!trackingId ? <p className="mt-1 text-sm text-white/60">
                        Track your shipment!
                    </p> : null }
                </div>
            </div>

            {loading ? (
                <span className="h-8 w-8 animate-pulse rounded-lg"></span>
            ) : error ? (
                    <div>
                        <div role="alert"
                             className="mb-4 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-100">
                            <div className="font-medium text-red-100">Failed to load shipment</div>
                            <div className="text-red-100/80">{error}</div>
                        </div>
                        <div className="flex justify-center">
                            <button
                                onClick={() => {
                                    setError("");
                                    setShipment(null);
                                    navigate("/track", { replace: true });
                                }}
                                className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-left text-sm font-medium text-white transition hover:bg-white/10 cursor-pointer"
                            >
                            <span className="grid h-8 w-8 place-items-center rounded-lg bg-indigo-500/20 text-indigo-300">
                                &#8592;
                            </span>
                                Return
                            </button>
                        </div>

                    </div>

            ) : !trackingId ? (
                <TrackShipment newTrackingId={newTrackingId} setNewTrackingId={setNewTrackingId} onSubmit={handleTrack} />
            ) : (
                <TrackingDetails />
            )}


        </div>
    )
}