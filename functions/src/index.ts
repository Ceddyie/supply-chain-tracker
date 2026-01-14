import { onCall, HttpsError } from "firebase-functions/v2/https";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";

initializeApp();

interface SetRoleData {
  uid: string;
  role: string;
}

export const setUserRole = onCall<SetRoleData>(async (request) => {
  // Prüfen ob User authentifiziert ist
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "User must be authenticated");
  }

  const { uid, role } = request.data;

  // Nur der User selbst darf seine Rolle setzen
  if (request.auth.uid !== uid) {
    throw new HttpsError("permission-denied", "Can only set own role");
  }

  // Rolle validieren
  if (!["SENDER", "STATION", "CUSTOMER"].includes(role)) {
    throw new HttpsError("invalid-argument", "Invalid role");
  }

  try {
    await getAuth().setCustomUserClaims(uid, { role });
    return { success: true, role };
  } catch (error) {
    console.error("Error setting role:", error);
    throw new HttpsError("internal", "Failed to set role");
  }
});
