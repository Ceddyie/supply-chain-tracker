import {initializeApp} from 'firebase/app';
import { getAuth, connectAuthEmulator } from 'firebase/auth';
import {getFunctions, connectFunctionsEmulator, httpsCallable} from 'firebase/functions';

const firebaseConfig = {
    apiKey: "demo-api-key",
    authDomain: "demo-api-key.firebaseapp.com",
    projectId: "local-project",
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const functions = getFunctions(app);

if (import.meta.env.DEV) {
    connectAuthEmulator(auth, "http://localhost:9099");
    connectFunctionsEmulator(functions, "localhost", 5001);
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
