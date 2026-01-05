import {useAuth} from "../context/AuthContext.tsx";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import {shipmentService} from "../services/api.ts";
import * as React from "react";

interface ShipmentStats {
    total: number;
    inTransit: number;
    delivered: number;
    pending: number;
}

type UserRole = "SENDER" | "STATION" | "CUSTOMER" | "ADMIN" | null;

export default function Dashboard() {
    const {user, role} = useAuth() as { user: any; role: UserRole };
    const navigate = useNavigate();

    const [stats, setStats] = useState<ShipmentStats>({
        total: 0,
        inTransit: 0,
        delivered: 0,
        pending: 0,
    });
    const [loading, setLoading] = useState(true);
    const [trackingId, setTrackingId] = useState("");

    useEffect(() => {
        if (role === "SENDER" || role === "ADMIN") {
            loadStats();
        } else {
            setLoading(false);
        }
    }, [role]);

    const loadStats = async () => {
        try {
            const response = await shipmentService.getMyShipments();
            const shipments = response.data;
            setStats({
                total: shipments.length,
                inTransit: shipments.filter((s: any) => s.currentStatus !== "DELIVERED" && s.currentStatus !== "CREATED").length,
                delivered: shipments.filter((s: any) => s.currentStatus === "DELIVERED").length,
                pending: shipments.filter((s: any) => s.currentStatus === "CREATED").length,
            });
        } catch (error) {
            console.error("Failed to load stats", error);
        } finally {
            setLoading(false);
        }
    };

    const handleTrack = (e: React.FormEvent) => {
        e.preventDefault();
        if (trackingId.trim()) {
            navigate(`/track/${trackingId}`);
        }
    };


    const statCards = [
        {
            label: "Total Shipments",
            value: stats.total,
            color: "bg-indigo-500/20 border-indigo-500/30 text-indigo-300",
            icon: "📦"
        },
        {
            label: "In Transit",
            value: stats.inTransit,
            color: "bg-amber-500/20 border-amber-500/30 text-amber-300",
            icon: "🚚",
        },
        {
            label: "Delivered",
            value: stats.delivered,
            color: "bg-emerald-500/20 border-emerald-500/30 text-emerald-300",
            icon: "✓",
        },
        {
            label: "Pending",
            value: stats.pending,
            color: "bg-slate-500/20 border-slate-500/30 text-slate-300",
            icon: "⏳",
        },
    ];

    const RoleBadge = ({userRole}: { userRole: UserRole }) => {
        const roleConfig: Record<string, { bg: string; text: string; label: string }> = {
            SENDER: {bg: "bg-indigo-500/20", text: "text-indigo-300", label: "SENDER"},
            STATION: {bg: 'bg-amber-500/20', text: 'text-amber-300', label: 'Station'},
            CUSTOMER: {bg: 'bg-cyan-500/20', text: 'text-cyan-300', label: 'Customer'},
            ADMIN: {bg: 'bg-emerald-500/20', text: 'text-emerald-300', label: 'Admin'},
        };
        const config = userRole ? roleConfig[userRole] : null;
        if (!config) return null;

        return (
            <span
                className={`inline-flex items-center rounded-lg ${config} ${config.text} px-2.5 py-1 text-xs font-medium`}>
                {config.label}
            </span>
        );
    };

    const SenderDashboard = () => (
        <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-2">
                {statCards.map((card) => (
                    <div
                        key={card.label}
                        className={`rounded-xl border ${card.color} p-4 transition hover:scale-[1.02]`}
                    >
                        <div className="flex items-center justify-between">
                            <span className="text-2xl">{card.icon}</span>
                            {loading ? (
                                <span className="h-8 w-8 animate-pulse rounded-lg bg-white/10"/>
                            ) : (
                                <span className="text-2xl font-bold text-white">
                                    {card.value}
                                </span>
                            )}
                        </div>
                        <div className="mt-2 text-sm font-medium">{card.label}</div>
                    </div>
                ))}
            </div>

            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
                <h2 className="text-lg font-medium text-white">Quick Actions</h2>
                <p className="mt-1 text-sm text-white/50">
                    Manage your shipments
                </p>
                <div className="mt-4 grid gap-2 sm:grid-cols-2">
                    <button
                        onClick={() => navigate("/shipments/new")}
                        className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-left text-sm font-medium text-white transition hover:bg-white/10 cursor-pointer"
                    >
                        <span className="grid h-8 w-8 place-items-center rounded-lg bg-indigo-500/20 text-indigo-300">
                            +
                        </span>
                        Create New Shipment
                    </button>
                    <button
                        onClick={() => navigate("/shipments")}
                        className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-left text-sm font-medium text-white transition hover:bg-white/10 cursor-pointer"
                    >
                        <span className="grid h-8 w-8 place-items-center rounded-lg bg-cyan-500/20 text-cyan-300">
                            📋
                        </span>
                        View All Shipments
                    </button>
                </div>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-lg font-medium text-white">Recent Activity</h2>
                        <p className="mt-1 text-sm text-white/50">
                            Your latest shipment updates
                        </p>
                    </div>
                    <button
                        onClick={() => navigate("/shipments")}
                        className="text-sm font-medium text-indigo-400 hover:text-indigo-300 transition cursor-pointer"
                    >
                        View all →
                    </button>
                </div>

                <div className="mt-4">
                    {loading ? (
                        <div className="space-y-3">
                            {[1, 2, 3].map((i) => (
                                <div
                                    key={i}
                                    className="h-16 animate-pulse rounded-xl bg-white/5"
                                />
                            ))}
                        </div>
                    ) : stats.total === 0 ? (
                        <div className="rounded-xl border border-dashed border-white/10 py-8 text-center">
                            <p className="text-white/40">No shipments yet</p>
                            <button
                                onClick={() => navigate("/shipments/new")}
                                className="mt-2 text-sm font-medium text-indigo-400 hover:text-indigo-300 transition cursor-pointer"
                            >
                                Create your first shipment →
                            </button>
                        </div>
                    ) : (
                        <p className="text-sm text-white/50">
                            You have {stats.total} shipment{stats.total !== 1 && "s"}.
                            View all shipments to see detailed activity.
                        </p>
                    )}
                </div>
            </div>
        </div>
    );

    const StationDashboard = () => (
        <div className="space-y-6">
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
                <h2 className="text-xl font-medium text-white">Station Control</h2>
                <p className="mt-1 text-sm text-white/50">
                    Update shipment statuses as they pass through your station
                </p>
                <div className="mt-6">
                    <button
                        onClick={() => navigate("/update")}
                        className="flex items-center gap-3 rounded-xl bg-amber-500/20 border border-amber-500/30 px-5 py-4 text-left font-medium text-white transition hover:bg-amber-500/30 cursor-pointer w-full sm:w-auto"
                    >
                        <span className="grid h-10 w-10 place-items-center rounded-lg bg-amber-500/30 text-amber-300 text-xl">
                            📍
                        </span>
                        <div>
                            <div className="text-amber-100">Send Tracking Update</div>
                            <div className="text-sm text-amber-300/70">Scan or enter shipment ID to update status</div>
                        </div>
                    </button>
                </div>
            </div>

            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
                <h2 className="text-lg font-medium text-white">Quick Track</h2>
                <p className="mt-1 text-sm text-white/50">
                    Look up a shipment's current status
                </p>
                <form onSubmit={handleTrack} className="mt-4 flex gap-2">
                    <input
                        type="text"
                        value={trackingId}
                        onChange={(e) => setTrackingId(e.target.value)}
                        placeholder="Enter tracking ID..."
                        className="flex-1 rounded-xl border border-white/10 bg-white/5 px-4 py-2.5 text-white placeholder:text-white/30 outline-none transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                    />
                    <button
                        type="submit"
                        disabled={!trackingId.trim()}
                        className="rounded-xl bg-indigo-500 px-5 py-2.5 font-medium text-white shadow-lg shadow-indigo-500/20 transition hover:bg-indigo-400 disabled:opacity-50 disabled:hover:bg-indigo-500 disabled:cursor-not-allowed cursor-pointer"
                    >
                        Track
                    </button>
                </form>
            </div>
        </div>
    );

    const CustomerDashboard = () => (
        <div className="space-y-6">
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
                <h2 className="text-xl font-medium text-white">Track Your Shipment</h2>
                <p className="mt-1 text-sm text-white/50">
                    Enter your tracking ID to see the current status of your package
                </p>
                <form onSubmit={handleTrack} className="mt-4 flex flex-col sm:flex-row gap-2">
                    <input
                        type="text"
                        value={trackingId}
                        onChange={(e) => setTrackingId(e.target.value)}
                        placeholder="Enter tracking ID (e.g., PKG-ABC123)"
                        className="flex-1 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                    />
                    <button
                        type="submit"
                        disabled={!trackingId.trim()}
                        className="rounded-xl bg-indigo-500 px-6 py-3 font-medium text-white shadow-lg shadow-indigo-500/20 transition hover:bg-indigo-400 disabled:opacity-50 disabled:hover:bg-indigo-500 disabled:cursor-not-allowed cursor-pointer"
                    >
                        Track Package
                    </button>
                </form>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-xl border border-white/10 bg-white/5 p-5">
                    <div className="flex items-center gap-3">
                        <span className="grid h-10 w-10 place-items-center rounded-lg bg-cyan-500/20 text-cyan-300 text-xl">
                            📦
                        </span>
                        <div>
                            <h3 className="font-medium text-white">Package Updates</h3>
                            <p className="text-sm text-white/50">Track in real-time</p>
                        </div>
                    </div>
                    <p className="mt-3 text-sm text-white/60">
                        Get instant updates on your shipment's location and estimated delivery time.
                    </p>
                </div>

                <div className="rounded-xl border border-white/10 bg-white/5 p-5">
                    <div className="flex items-center gap-3">
                        <span className="grid h-10 w-10 place-items-center rounded-lg bg-emerald-500/20 text-emerald-300 text-xl">
                            ✓
                        </span>
                        <div>
                            <h3 className="font-medium text-white">Delivery Status</h3>
                            <p className="text-sm text-white/50">Know when it arrives</p>
                        </div>
                    </div>
                    <p className="mt-3 text-sm text-white/60">
                        Receive notifications when your package is out for delivery or delivered.
                    </p>
                </div>
            </div>
        </div>
    );

    const renderDashboard = () => {
        switch (role) {
            case "SENDER":
                return <SenderDashboard />;
            case "STATION":
                return <StationDashboard />;
            case "CUSTOMER":
                return <CustomerDashboard />;
            case "ADMIN":
                return <SenderDashboard />;
            default:
                return <CustomerDashboard />;
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex items-start justify-between">
                <div>
                    <h1 className="text-2xl font-semibold tracking-tight text-white">
                        Dashboard
                    </h1>
                    <p className="mt-1 text-sm text-white/60">
                        Welcome back, {user?.email ?? "User"}!
                    </p>
                </div>
                <RoleBadge userRole={role} />
            </div>

            {renderDashboard()}
        </div>
    )
}