import * as React from "react";

export function TrackShipment({
                           newTrackingId,
                           setNewTrackingId,
                           onSubmit
                       }: {
    newTrackingId: string;
    setNewTrackingId: (v: string) => void;
    onSubmit: (e: React.FormEvent) => void;
}) {
    return (
        <div className="rounded-xl border border-white/10 bg-white/5 p-5">
            <h2 className="text-xl font-medium text-white">Track Your Shipment</h2>
            <p className="mt-1 text-sm text-white/50">
                Enter your tracking ID to see the current status of your package
            </p>
            <form onSubmit={onSubmit} className="mt-4 flex flex-col sm:flex-row gap-2">
                <input
                    type="text"
                    value={newTrackingId}
                    onChange={(e) => setNewTrackingId(e.target.value)}
                    placeholder="Enter tracking ID (e.g., PKG-ABC123)"
                    className="flex-1 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                />
                <button
                    type="submit"
                    disabled={!newTrackingId.trim()}
                    className="rounded-xl bg-indigo-500 px-6 py-3 font-medium text-white shadow-lg shadow-indigo-500/20 transition hover:bg-indigo-400 disabled:opacity-50 disabled:hover:bg-indigo-500 disabled:cursor-not-allowed cursor-pointer"
                >
                    Track Package
                </button>
            </form>
        </div>
    );
}