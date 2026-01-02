import {
    createUserWithEmailAndPassword,
    type User,
    onAuthStateChanged,
    signInWithEmailAndPassword,
    signOut
} from "firebase/auth";
import {createContext, type ReactNode, useContext, useEffect, useState} from "react";
import {auth, setUserRole} from "../config/firebase";

type UserRole = 'SENDER' | 'STATION' | 'CUSTOMER' | 'ADMIN' | null;

interface AuthContextType {
    user: User | null;
    role: UserRole;
    //token: string | null;
    loading: boolean;
    login: (email: string, password: string) => Promise<void>;
    register: (email: string, password: string, accountType?: "CUSTOMER" | "SENDER") => Promise<void>;
    logout: () => Promise<void>;
    refreshRole: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children}: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [role, setRole] = useState<UserRole>(null);
    const [loading, setLoading] = useState(true);

    const fetchRole = async (currentUser: User | null): Promise<UserRole> => {
        if (!currentUser) return null;

        try {
            const tokenResult = await currentUser.getIdTokenResult(true);
            return (tokenResult.claims.role as UserRole) || null;
        } catch (error) {
            console.error("Error fetching role:", error);
            return null;
        }
    };

    const refreshRole = async () => {
        if (user) {
            const newRole = await fetchRole(user);
            setRole(newRole);
        }
    }

    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, async (user) => {
            setUser(user);
            if (user) {
                const userRole = await fetchRole(user);
                setRole(userRole);

                if (!userRole) {
                    let attempts = 0;
                    const maxAttempts = 5;
                    const pollInterval = setInterval(async () => {
                        attempts++;
                        const polledRole = await fetchRole(user);
                        if (polledRole || attempts >= maxAttempts) {
                            setRole(polledRole);
                            clearInterval(pollInterval);
                        }
                    }, 1000);
                }
            } else {
                setRole(null);
            }
            setLoading(false);
        });

        return () => unsubscribe();
    }, []);

    const login = async (email: string, password: string) => {
        const userCredential = await signInWithEmailAndPassword(auth, email, password);
        if (userCredential.user) {
            const userRole = await fetchRole(userCredential.user);
            setRole(userRole);
        }
    };

    const register = async (email: string, password: string, accountType: "CUSTOMER" | "SENDER" = "CUSTOMER") => {
        const userCredential = await createUserWithEmailAndPassword(auth, email, password);
        try {
            await setUserRole({uid: userCredential.user.uid, role: accountType});
        } catch (error) {
            console.error("Failed to set user role via Cloud Function:", error);
        }

        await new Promise(resolve => setTimeout(resolve, 1500));
        if (userCredential.user) {
            const userRole = await fetchRole(userCredential.user);
            setRole(userRole);
        }
    };

    const logout = async () => {
        await signOut(auth);
        setRole(null);
    }

    const value: AuthContextType = {
        user,
        role,
        loading,
        register,
        login,
        logout,
        refreshRole,
    };

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
}