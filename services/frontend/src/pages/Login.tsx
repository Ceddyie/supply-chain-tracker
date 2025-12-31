import {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {useAuth} from "../context/AuthContext.tsx";
import * as React from "react";

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPw, setShowPw] = useState(false);

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await login(email, password);
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="min-h-screen w-full bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center px-4">
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
                <div className="absolute -top-24 -left-24 h-72 w-72 rounded-full bg-cyan-500/20 blur-3xl"/>
                <div className="absolute -bottom-24 -right-24 h-72 w-72 rounded-full bg-cyan-500/20 blur-3xl"/>
            </div>

            <div className="relative w-full max-w-md">
                <div className="rounded-2xl border border-white/10 bg-white/5 shadow-2xl backdrop-blur-xl">
                    <div className="px-6 pt-7 pb-6 sm:px-8">
                        <div className="mb-6">
                            <h1 className="text-2xl font-semibold tracking-tight text-white">
                                Welcome back
                            </h1>
                            <p className="mt-1 text-sm text-white/60">
                                Sign in to continue to your dashboard
                            </p>
                        </div>
                        {error && (
                            <div role="alert"
                                 className="mb-4 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-100"
                            >
                                <div className="font-medium text-red-100">Login failed</div>
                                <div className="text-red-100/80">Please make sure you entered the correct credentials!</div>
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-white/80">
                                    E-Mail
                                </label>
                                <div className="mt-2">
                                    <input
                                        type="email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        placeholder="you@example.com"
                                        autoComplete="email"
                                        required
                                        className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/30 outline-none ring-0 transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-white/80">
                                    Password
                                </label>
                                <div className="mt-2 relative">
                                    <input
                                        type={showPw ? "text" : "password"}
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        placeholder="••••••••"
                                        autoComplete="current-password"
                                        required
                                        className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 pr-16 text-white placeholder:text-white/30 outline-none transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPw((v) => !v)}
                                        className="absolute right-2 top-1/2 -translate-y-1/2 rounded-lg px-3 py-1.5 text-xs font-medium text-white/70 hover:text-white hover:bg-white/10 transition"
                                        aria-label={showPw ? "Hide password" : "Show password"}
                                    >
                                        {showPw ? "Hide" : "Show"}
                                    </button>
                                </div>

                                <div className="mt-2 flex items-center justify-between">
                                    <span className="text-xs text-white/40">
                                    </span>
                                    {/* TODO: RESET Page */}
                                </div>
                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                className="group w-full rounded-xl bg-indigo-500 px-4 py-3 font-medium text-white shadow-lg shadow-indigo-500/20 transition hover:bg-indigo-400 disabled:opacity-60 disabled:hover:bg-indigo-500"
                            >
                <span className="flex items-center justify-center gap-2">
                  {loading && (
                      <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white"/>
                  )}
                    {loading ? "Signing in..." : "Login"}
                </span>
                            </button>
                        </form>

                        <div className="mt-6 flex items-center gap-3">
                            <div className="h-px flex-1 bg-white/10"/>
                            <span className="text-xs text-white/40">or</span>
                            <div className="h-px flex-1 bg-white/10"/>
                        </div>

                        <p className="mt-4 text-center text-sm text-white/60">
                            Not yet registered?{" "}
                            <Link
                                to="/register"
                                className="font-medium text-indigo-300 hover:text-indigo-200 transition"
                            >
                                Create an account
                            </Link>
                        </p>
                    </div>
                </div>

                <p className="mt-4 text-center text-xs text-white/30">
                    By continuing, you agree to the app’s terms and privacy policy.
                </p>
            </div>
        </div>
    );
}