import * as React from "react";
import {NavLink, useNavigate} from "react-router-dom";
import {useAuth} from "../context/AuthContext.tsx";

const navItems = [
    { to: '/dashboard', label: 'Dashboard' },
];

export default function Navbar() {
    const [open, setOpen] = React.useState(false);
    const navigate = useNavigate();

    const { user, logout } = useAuth() as any;

    const linkClass = ({ isActive }: { isActive?: boolean }) =>
        [
            "rounded-xl px-3 py-2 text-sm font-medium transition",
            isActive
                ? "bg-white/10 text-white"
                : "text-white/70 hover:text-white hover:bg-white/10",
        ].join(" ");

    const onLogout = async () => {
        try {
            await logout?.();
            navigate("/login");
        } catch {

        }
    };

    return (
        <header className="sticky top-0 z-40 border-b border-white/10 bg-slate-950/40 backdrop-blur-xl">
            <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => navigate("/dashboard")}
                        className="group flex items-center gap-2 rounded-xl px-2 py-1 hover:bg-white/5 transition cursor-pointer"
                        >
                        <div className="h9 w-9 rounded-xl bg-indigo-500/20 border border-white/10 grid place-items-center">
                            <span className="text-sm font-bold text-white">SCT</span>
                        </div>
                        <div className="leading-tight">
                            <div className="text-sm font-semibold">Supply Chain Tracker</div>
                            <div className="text-xs text-white/40">Backoffice</div>
                        </div>
                    </button>
                </div>

                <div className="flex items-center gap-2">
                    {user && (
                        <div className="hidden sm:flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2">
                            <div className="h-7 w-7 rounded-lg bg-white/10 grid place-items-center text-xs font-semibold">
                                {String(user?.email || "U").slice(0,1).toUpperCase()}
                            </div>
                            <div className="max-w-[180px] truncate text-sm text-white/70">
                                {user?.email ?? "Signed in"}
                            </div>
                        </div>
                    )}

                    {user ? (
                        <button
                            onClick={onLogout}
                            className="hidden sm:inline-flex rounded-xl bg-white/10 px-3 py-2 text-sm font-medium text-white hover:bg-white/15 transition cursor-pointer"
                            >
                            Logout
                        </button>
                    ) : (
                        <button
                            onClick={() => navigate("/login")}
                            className="hidden sm:inline-flex rounded-xl bg-indigo-500 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-400 transition cursor-pointer"
                            >
                            Login
                        </button>
                    )}

                    <button
                        onClick={() => setOpen((v) => !v)}
                        className="md:hidden rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm text-white/80 hover:bg-white/10 transition cursor-pointer"
                        aria-label="Toggle menu"
                        >
                        {open ? "Close" : "Menu"}
                    </button>
                </div>
            </div>

            {open && (
                <div className="d:hidden border-t border-white/10 bg-slate-950/30 backdrop-blur-xl">
                    <div className="mx-auto w-full max-w-6xl px-4 py-3 sm:px-6 space-y-2">
                        <div className="grid gap-1">
                            {navItems.map((i) => (
                                <NavLink
                                key={i.to}
                                to={i.to}
                                className={linkClass}
                                onClick={() => setOpen(false)}
                                end
                                >
                                    {i.label}
                                </NavLink>
                            ))}
                        </div>

                        <div className="flex items-center justify-between pt-2 border-t border-white/10">
                            <div className="text-xs text-white/50 truncate">
                                {user?.email ?? "Not signed in"}
                            </div>

                            {user ? (
                                <button
                                    onClick={async () => {
                                        setOpen(false)
                                        await onLogout();
                                    }}
                                    className="rounded-xl bg-white/10 px-3 py-2 text-sm font-medium text-white hover:bg-white/15 transition cursor-pointer"
                                    >
                                    Logout
                                </button>
                            ) : (
                                <button
                                    onClick={() => {
                                        setOpen(false)
                                        navigate("/login");
                                    }}
                                    className="rounded-xl bg-indigo-500 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-400 transition cursor-pointer"
                                    >
                                    Login
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </header>
    );
}