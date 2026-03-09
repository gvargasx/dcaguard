import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useRef,
  type ReactNode,
} from "react";

import {
  onAuthStateChanged,
  signOut,
  User as FirebaseUser,
  GoogleAuthProvider,
  signInWithPopup,
  signInAnonymously,
} from "firebase/auth";

import type { User } from "../types";
import { auth } from "../firebase";
import { authApi } from "../services/api";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  error: string | null;
  loginWithGoogle: () => Promise<void>;
  loginAnonymously: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  loading: true,
  error: null,
  loginWithGoogle: async () => {},
  loginAnonymously: async () => {},
  logout: async () => {},
});

async function syncBackendUser(firebaseUser: FirebaseUser): Promise<User> {
  const token = await firebaseUser.getIdToken(true);
  localStorage.setItem("firebase_token", token);
  return authApi.login();
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const syncingRef = useRef(false);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (!firebaseUser) {
        localStorage.removeItem("firebase_token");
        setUser(null);
        setLoading(false);
        return;
      }

      if (syncingRef.current) return;
      syncingRef.current = true;

      try {
        setError(null);
        const backendUser = await syncBackendUser(firebaseUser);
        setUser(backendUser);
      } catch (e: any) {
        console.error("Backend sync failed:", e);
        setError(e?.message ?? "Authentication failed");
        setUser(null);
        localStorage.removeItem("firebase_token");
      } finally {
        syncingRef.current = false;
        setLoading(false);
      }
    });

    return () => unsubscribe();
  }, []);

  const loginWithGoogle = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: "select_account" });
      await signInWithPopup(auth, provider);
    } catch (e: any) {
      setError(e?.message ?? "Google login failed");
      setLoading(false);
    }
  }, []);

  const loginAnonymously = useCallback(async () => {
    setError(null);
    setLoading(true);
    await signInAnonymously(auth);
  }, []);

  const logout = useCallback(async () => {
    await signOut(auth);
    localStorage.removeItem("firebase_token");
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{ user, loading, error, loginWithGoogle, loginAnonymously, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);