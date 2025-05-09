import { useState, useCallback, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faSignOutAlt,
  faUserCircle,
  faPenFancy,
} from "@fortawesome/free-solid-svg-icons";
import axios from "axios";
export default function Navbar() {
  
  const apiurl =import.meta.env.VITE_API_URL;
  const [userName, setUserName] = useState("Loading...");

  const token = localStorage.getItem("token");
  const headers = { Authorization: `Bearer ${token}` };

  const getUsername = useCallback(async () => {
    try {
      const { data } = await axios.get(`${apiurl}/api/users`, { headers });
      setUserName(data);
    } catch (error) {
      setUserName("Unknown User");
    }
  }, []);

  useEffect(() => {
    getUsername();
  }, [getUsername]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    window.location.reload();
  };

  return (
    <header className="sticky top-0 z-50 bg-gradient-to-r from-white via-gray-50 to-gray-100 border-b border-gray-200 w-full shadow-md">
      <nav className="w-full px-6 md:px-10 h-16 flex items-center justify-between">
        {/* Logo and App Name */}
        <div className="flex items-center gap-2 text-gray-800 text-xl font-semibold">
          <span className="select-none tracking-wide">NoteVerse</span>
          <FontAwesomeIcon icon={faPenFancy} className="text-gray-500 italic text-base" />
        </div>

        {/* Username and Logout */}
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
            <FontAwesomeIcon icon={faUserCircle} className="text-lg text-gray-500" />
            <span className="truncate">{userName}</span>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center justify-center w-9 h-9 rounded-full hover:scale-105 transition-transform"
            title="Logout"
          >
            <FontAwesomeIcon icon={faSignOutAlt} className="text-gray-600 text-base" />
          </button>
        </div>
      </nav>
    </header>
  );
}