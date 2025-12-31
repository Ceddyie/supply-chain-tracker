import {initializeApp} from 'firebase/app';
import { getAuth, connectAuthEmulator } from 'firebase/auth';

const firebaseConfig = {
    apiKey: "demo-api-key",
    authDomain: "demo-api-key.firebaseapp.com",
    projectId: "local-project",
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);

if (import.meta.env.DEV) {
    connectAuthEmulator(auth, "http://localhost:9099");
}
