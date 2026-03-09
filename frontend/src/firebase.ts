import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyCUIQ1NJKgbD5qrkdpwKd6PYCBz28DHDJc",
  authDomain: "dca-guard-dev.firebaseapp.com",
  projectId: "dca-guard-dev",
  storageBucket: "dca-guard-dev.firebasestorage.app",
  messagingSenderId: "328831244715",
  appId: "1:328831244715:web:5ae80e9eddddfbeb501aee"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);