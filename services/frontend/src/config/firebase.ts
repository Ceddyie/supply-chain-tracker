import {initializeApp} from 'firebase/app';
import { getAuth, connectAuthEmulator } from 'firebase/auth';
import {getFunctions, connectFunctionsEmulator, httpsCallable} from 'firebase/functions';

const useEmulator = (import.meta.env.VITE_USE_FIREBASE_EMULATOR ?? "false").toLowerCase() === "true";

const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY ?? "demo-api-key",
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN ?? "demo-api-key.firebaseapp.com",
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID ?? "local-project",
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const functions = getFunctions(app);

if (useEmulator) {
    const authEmulatorUrl = import.meta.env.VITE_FIREBASE_AUTH_EMULATOR_URL ?? "http://localhost:9099";
    const functionsHost = import.meta.env.VITE_FIREBASE_FUNCTIONS_EMULATOR_HOST ?? "localhost";
    const functionsPort = Number(import.meta.env.VITE_FIREBASE_FUNCTIONS_EMULATOR_PORT ?? "5001");

    connectAuthEmulator(auth, authEmulatorUrl);
    connectFunctionsEmulator(functions, functionsHost, functionsPort);
}

export const getUserRole = async (): Promise<string | null> => {
    const user = auth.currentUser;
    if (!user) return null;

    const tokenResult = await user.getIdTokenResult();
    return (tokenResult.claims.role as string) || null;
};

export const setUserRole = httpsCallable<{ uid: string; role: string }, { success: boolean }>(
    functions,
    'setUserRole',
);
