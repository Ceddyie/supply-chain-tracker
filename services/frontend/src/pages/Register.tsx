import {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {useAuth} from "../context/AuthContext.tsx";
import * as React from "react";

export default function Register() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState('');
    const [showPw, setShowPw] = useState(false);
    const [showPwConfirm, setShowPwConfirm] = useState(false);

    const [isBusinessAccount, setIsBusinessAccount] = useState(false);

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { register } = useAuth();
    const navigate = useNavigate();

    const isPasswordValid = password.length >= 8;
    const doPasswordsMatch = password === passwordConfirm;

    const canSubmit = isPasswordValid && doPasswordsMatch && email.length > 0;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const accountType = isBusinessAccount ? "SENDER" : "CUSTOMER";
            await register(email, password, accountType);
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.message || 'Registration failed');
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
                                Welcome
                            </h1>
                            <p className="mt-1 text-sm text-white/60">
                                Register an account to continue
                            </p>
                        </div>
                        {error && (
                            <div role="alert"
                                 className="mb-4 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-100"
                            >
                                <div className="font-medium text-red-100">Registration failed</div>
                                <div className="text-red-100/80">{error}</div>
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
                                        autoComplete="new-password"
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
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-white/80">
                                    Confirm Password
                                </label>
                                <div className="mt-2 relative">
                                    <input
                                        type={showPwConfirm ? "text" : "password"}
                                        value={passwordConfirm}
                                        onChange={(e) => setPasswordConfirm(e.target.value)}
                                        placeholder="••••••••"
                                        autoComplete="new-password"
                                        required
                                        minLength={8}
                                        className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 pr-16 text-white placeholder:text-white/30 outline-none transition focus:border-indigo-400/40 focus:bg-white/10 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.15)]"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPwConfirm((v) => !v)}
                                        className="absolute right-2 top-1/2 -translate-y-1/2 rounded-lg px-3 py-1.5 text-xs font-medium text-white/70 hover:text-white hover:bg-white/10 transition"
                                        aria-label={showPwConfirm ? "Hide password" : "Show password"}
                                    >
                                        {showPwConfirm ? "Hide" : "Show"}
                                    </button>
                                </div>

                                {password.length > 0 && password.length < 8 && (
                                    <p className="mt-1 text-xs text-red-400">
                                        Password must be at least 8 characters long.
                                    </p>
                                )}

                                {passwordConfirm.length > 0 && password !== passwordConfirm && (
                                    <p className="mt-1 text-xs text-red-400">
                                        Passwords do not match.
                                    </p>
                                )}

                                <div className="mt-2 flex items-center justify-between">
                                    <span className="text-xs text-white/40">
                                        Use at least 8 characters for your password.
                                    </span>
                                    {/* TODO: RESET Page */}
                                </div>
                            </div>

                            <div className="rounded-xl border border-white/10 bg-white/5 p-4">
                                <label className="flex items-center gap-3 cursor-pointer">
                                    <div className="relative flex items-center justify-center pt-0.5">
                                        <input
                                            type="checkbox"
                                            checked={isBusinessAccount}
                                            onChange={(e) => setIsBusinessAccount(e.target.checked)}
                                            className="peer sr-only"
                                        />
                                        <div className="h-5 w-5 rounded-md border border-white/20 bg-white/5 transition peer-checked:border-indigo-400 peer-checked:bg-indigo-500 peer-focus:ring-2 peer-focus:ring-indigo-400/20">
                                            {isBusinessAccount && (
                                                <svg className="h-5 w-5 text-white" viewBox="0 0 20 20" fill="currentColor">
                                                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                                </svg>
                                            )}
                                        </div>
                                    </div>
                                    <div className="flex-1">
                                        <div className="text-sm font-medium text-white">
                                            Business Account
                                        </div>
                                        <p className="mt-0.5 text-xs text-white/50">
                                            Enable this if you are a business or a seller who needs to create and manage shipments. Regular accounts can only track packages.
                                        </p>
                                    </div>
                                </label>
                            </div>

                            <button
                                type="submit"
                                disabled={!canSubmit || loading}
                                className="group w-full rounded-xl bg-indigo-500 px-4 py-3 font-medium text-white shadow-lg shadow-indigo-500/20 transition hover:bg-indigo-400 disabled:opacity-60 disabled:hover:bg-indigo-500 cursor-pointer"
                            >
                                <span className="flex items-center justify-center gap-2">
                                    {loading && (
                                        <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white"/>
                                    )}
                                    {loading ? "Creating..." : isBusinessAccount ? "Crate Business Account" : "Create Account"}
                                </span>
                            </button>
                        </form>

                        <div className="mt-6 flex items-center gap-3">
                            <div className="h-px flex-1 bg-white/10"/>
                            <span className="text-xs text-white/40">or</span>
                            <div className="h-px flex-1 bg-white/10"/>
                        </div>

                        <p className="mt-4 text-center text-sm text-white/60">
                            Already registered?{" "}
                            <Link
                                to="/login"
                                className="font-medium text-indigo-300 hover:text-indigo-200 transition cursor-pointer"
                            >
                                Login here
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