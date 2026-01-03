import {useAuth} from "../context/AuthContext.tsx";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import * as React from "react";
import {shipmentService} from "../services/api.ts";
import {toast} from "sonner";

type UserRole = "SENDER" | "STATION" | "CUSTOMER" | "ADMIN" | null;

export default function CreateShipment () {
    const { user, role } = useAuth() as { user: any, role: UserRole };
    const navigate = useNavigate();

    const [sender, setSender] = useState(user.email);
    const [receiver, setReceiver] = useState("");
    const [receiverStreet, setReceiverStreet] = useState("");
    const [receiverCity, setReceiverCity] = useState("");
    const [expectedDelivery, setExpectedDelivery] = useState("");

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const expectedDeliveryInstant = new Date(`${expectedDelivery}T00:00:00Z`).toISOString();
            const res = await shipmentService.create({sender, receiver, receiverStreet, receiverCity, expectedDelivery: expectedDeliveryInstant});
            console.log(res)
            const trackingId = res.data.trackingId ?? res.data.trackingID;

            toast.success("Shipment created", {
                description: `Tracking ID: ${trackingId}`,
            });

            navigate("/dashboard");
        } catch (err: any) {
            setError(err.message || "Shipment creation failed");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (!(role === "SENDER" || role === "ADMIN")) {
            navigate("/dashboard");
        }
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
                        Create New Shipment
                    </h1>
                    <p className="mt-1 text-sm text-white/60">
                        Enter receiver details and the expected delivery date.
                    </p>
                </div>
            </div>
            <div className="px-6 pt-7 pb-6 sm:px-8">
                {error && (
                    <div role="alert"
                         className="mb-4 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-100">
                        <div className="font-medium text-red-100">Shipment creation failed</div>
                        <div className="text-red-100/80">{error}</div>
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Sender
                            </label>
                            <div className="mt-2">
                                <input
                                    type="text"
                                    value={sender}
                                    onChange={(e) => setSender(e.target.value)}
                                    placeholder="Sender Identifier"
                                    autoComplete="email"
                                    required
                                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Receiver
                            </label>
                            <div className="mt-2">
                                <input
                                    type="text"
                                    value={receiver}
                                    onChange={(e) => setReceiver(e.target.value)}
                                    placeholder="Shipment Receiver"
                                    autoComplete="email"
                                    required
                                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Receiver Address
                            </label>
                            <div className="mt-2">
                                <input
                                    type="text"
                                    value={receiverStreet}
                                    onChange={(e) => setReceiverStreet(e.target.value)}
                                    placeholder="Any Street 123"
                                    autoComplete="street-address"
                                    required
                                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Receiver City
                            </label>
                            <div className="mt-2">
                                <input
                                    type="text"
                                    value={receiverCity}
                                    onChange={(e) => setReceiverCity(e.target.value)}
                                    placeholder="12345 Any City"
                                    autoComplete="address-level1"
                                    required
                                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-white/80">
                                Expected Delivery
                            </label>
                            <div className="mt-2">
                                <input
                                    type="date"
                                    value={expectedDelivery}
                                    onChange={(e) => setExpectedDelivery(e.target.value)}
                                    autoComplete=""
                                    required
                                    className="block w-full appearance-none min-h-[48px]å rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                />
                            </div>
                        </div>
                    </div>

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
                                    {loading ? "Creating..." : "Create Shipment"}
                                </span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}