import {Navigate, Outlet,} from "react-router-dom";
import {useAuth} from "../context/AuthContext.tsx";

/*type Props = {
    redirectTo?: string;
    children?: React.ReactNode;
};*/

export default function PrivateRoute () {
    const { user, loading } = useAuth() as any;

    if (loading) {
        return (
            <div className="min-h-[60vh] grid place-items-center">
                <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-white/5 px-5 py-4 shadow-2xl backdrop-blur-xl">
                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                    <span className="text-sm text-white/70">Loading...</span>
                </div>
            </div>
        );
    }

    return user ? <Outlet /> : <Navigate to="/login" replace />;
}