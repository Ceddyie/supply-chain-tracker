import {useAuth} from "../context/AuthContext.tsx";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import {shipmentService} from "../services/api.ts";
import {ErrorDiv} from "../components/ErrorDiv.tsx";

type UserRole = "SENDER" | "STATION" | "CUSTOMER" | "ADMIN" | null;

type ShipmentListItem = {
    id: string;
    trackingId: string;
    sender: string;
    receiver: string;
    currentStatus: string;
    expectedDelivery: Date;
    createdAt: Date;
}

export default function MyShipments() {
    const { user, role } = useAuth() as { user: any; role: UserRole };
    const navigate = useNavigate();

    const [shipments, setShipments] = useState<ShipmentListItem[]>([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadShipments = async () => {
        if (!user) return;

        setLoading(true);
        setError("");

        try {
            const res = await shipmentService.getMyShipments();
            setShipments(res.data);
        } catch (err: any) {
            setError(err.message || "Something went wrong...");
            console.error("Failed to load shipments", err);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (!user) return;
        if (role !== "SENDER" && role !== "ADMIN") {
            navigate("/dashboard");
        }
        loadShipments();
    }, [user, role]);

    return (
        <div className="space-y-6">
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
                        Your Shipments
                    </h1>
                    <p className="mt-1 text-sm text-white/60">
                        See a list with details on all your shipments.
                    </p>
                </div>
            </div>

            <div className="pt-7 pb-6">
                {error && <ErrorDiv error={error} />}

                {loading ? (
                    <span className="h-8 w-8 animate-pulse rounded-lg"></span>
                ) : shipments.length > 0 ? (
                        shipments.map((shipment) => (
                            <div
                                key={shipment.id} className="rounded-xl border border-white/10 bg-white/5 p-5 mb-4 cursor-pointer transition hover:scale-[1.02]"
                                onClick={() => navigate(`/track/${shipment.trackingId}`)}
                            >
                                <div className="flex items-center gap-3 sm:flex-row sm:items-center sm:justify-between">
                                    <div>
                                        <h1 className="text-xl text-white">{shipment.trackingId}</h1>
                                        <div className="flex">
                                            <p className="text-sm text-white/60">Receiver:</p>
                                            <p className="text-sm text-white/70 ml-1">{shipment.receiver}</p>
                                        </div>
                                    </div>

                                    <div className="sm:text-right flex items-center gap-2">
                                        <span className={`rounded-full border ${shipment.currentStatus === "CREATED" ? "bg-slate-500/20 border-slate-500/30 text-slate-300" : shipment.currentStatus === "DELIVERED" ? "bg-emerald-500/20 border-emerald-500/30 text-emerald-300" : "border-indigo-400/40 bg-indigo-500/25 text-indigo-200"} px-1.5 py-0.5 text-xs font-light select-none`}>
                                            {shipment.currentStatus.replaceAll("_", " ")}
                                        </span>
                                        <span className="grid h-11 w-8 place-items-center rounded-lg bg-indigo-500/20 text-indigo-300">
                                            &#62;
                                        </span>
                                    </div>
                                </div>
                            </div>
                        ))
                        ) : (
                            <div>
                                <h2 className="text-center">No shipments yet.</h2>
                            </div>
                        )
                }
            </div>
        </div>
    )
}