//import {HttpsError, onCall} from "firebase-functions/v2/https";
import * as functions from 'firebase-functions/v1';
import * as admin from 'firebase-admin';

admin.initializeApp();

type Role = 'CUSTOMER' | 'SENDER' | 'STATION' | 'ADMIN';

const SELF_ASSIGNABLE_ROLES = ['CUSTOMER', 'SENDER'];
const ALL_VALID_ROLES = ['CUSTOMER', 'SENDER', 'STATION', 'ADMIN'];

async function setRoleSafely(uid: string, role: Role) {
    const user = await admin.auth().getUser(uid);
    const existingClaims = user.customClaims || {};

    await admin.auth().setCustomUserClaims(uid, {
        ...existingClaims,
        role
    });
}

export const setUserRole = functions
    .region("us-central1")
    .https.onCall(async (data, context) => {
        const role = data?.role as Role | undefined;

        if (!context.auth) {
            throw new functions.https.HttpsError("unauthenticated", "Not logged in");
        }
        if (!role || !SELF_ASSIGNABLE_ROLES.includes(role)) {
            throw new functions.https.HttpsError(
                "permission-denied",
                "You can only register as CUSTOMER or SENDER"
            );
        }

        await setRoleSafely(context.auth.uid, role);
        return { success: true, role };
    });


export const adminSetUserRole = functions
    .region("us-central1")
    .https.onCall(async (data, context) => {
        const { targetUid, newRole } = data as { targetUid?: string; newRole?: Role };

        if (!context.auth) {
            throw new functions.https.HttpsError("unauthenticated", "Not logged in");
        }

        const caller = await admin.auth().getUser(context.auth.uid);
        if (caller.customClaims?.role !== "ADMIN") {
            throw new functions.https.HttpsError("permission-denied", "Admin only");
        }
        if (!targetUid || !newRole || !ALL_VALID_ROLES.includes(newRole)) {
            throw new functions.https.HttpsError("invalid-argument", "Invalid input");
        }

        await setRoleSafely(targetUid, newRole);
        return { success: true };
    });


export const onUserCreate = functions.auth.user().onCreate(async (user) => {
    const fullUser = await admin.auth().getUser(user.uid);
    if (fullUser.customClaims?.role) return;
    await admin.auth().setCustomUserClaims(user.uid, { role: "CUSTOMER" });
});