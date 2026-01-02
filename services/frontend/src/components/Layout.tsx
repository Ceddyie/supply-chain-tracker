import * as React from 'react';
import Navbar from "./Navbar.tsx";

type Props = {
    children: React.ReactNode;
};

export default function Layout({ children }: Props) {
    return (
        <div className="min-h-screen w-full bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950 text-white">
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
                <div className="absolute -top-24 -left-24 h-80 w-80 rounded-full bg-indigo-500/20 blur-3xl" />
                <div className="absolute -bottom-24 -right-24 h-80 w-80 rounded-full bg-cyan-500/20 blur-3xl" />
            </div>

            <div className="relative">
                <Navbar />

                <main className="mx-auto w-full max-w-6xl px-4 py-6 sm:py-6 sm:py-8">
                    <div className="rounded-2xl border border-white/10 bg-white/5 shadow-2xl backdrop-blur-xl">
                        <div className="p-4 sm:p-6">{children}</div>
                    </div>
                </main>

                <footer className="mx-auto w-full max-w-6xl px-4 pb-10 text-center text-xs text-white/30">
                    © {new Date().getFullYear()} · Supply Chain Tracker
                </footer>
            </div>
        </div>
    );
}