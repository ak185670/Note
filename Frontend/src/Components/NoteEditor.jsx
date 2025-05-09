import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faBold,
  faItalic,
  faUnderline,
  faCheckSquare,
  faImage,
  faSave,
  faTag,
  faTimes,
  faShareFromSquare,
  faUser,
  faClose,
} from "@fortawesome/free-solid-svg-icons";


const NoteEditor = ({ note, onSave, onClose }) => {
  
  
  const isEditing = Boolean(note?.id);
  const editorRef = useRef(null);
  const titleRef = useRef(null);
  const fileInput = useRef(null);

  const [tags, setTags] = useState([]);
  const [newTag, setNewTag] = useState("");

  const [showShareBox, setShowShareBox] = useState(false);
  const [sharedUsers, setSharedUsers] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);

  const token = localStorage.getItem("token");
  const headers = { Authorization: `Bearer ${token}` };

  const setPh = (ref, ph) => {
    if (ref.current.innerText.trim() === "") {
      ref.current.innerHTML = "&#8203;";
      ref.current.dataset.placeholder = ph;
    } else ref.current.dataset.placeholder = "";
  };

  const exec = (cmd) => document.execCommand(cmd, false, null);

  const insertCheckbox = () => {
    const cb = document.createElement("input");
    cb.type = "checkbox";
    editorRef.current.append(cb, " ");
  };

  const insertImage = (e) => {
    const f = e.target.files[0];
    if (!f) return;
    const r = new FileReader();
    r.onload = (ev) => {
      const img = document.createElement("img");
      img.src = ev.target.result;
      img.style.maxWidth = "100%";
      editorRef.current.append(img);
      fileInput.current.value = "";
    };
    r.readAsDataURL(f);
  };

  useEffect(() => {
    if (isEditing && note) {
      titleRef.current.innerText = note.title || "";
      editorRef.current.innerHTML = note.content || "";
      setTags(note.tags || []);
    } else {
      titleRef.current.innerText = "";
      editorRef.current.innerHTML = "";
      setTags([]);
    }
    setPh(titleRef, "Title");
    setPh(editorRef, "Take a note…");
  }, [note, isEditing]);

  const addTag = () => {
    const t = newTag.trim();
    if (t && !tags.includes(t)) setTags([...tags, t]);
    setNewTag("");
  };

  const removeTag = (tagToRemove) =>
    setTags(tags.filter((t) => t !== tagToRemove));

  const saveNote = async () => {
    const title = titleRef.current.innerText.trim().replace(/\u200B/g, "");
    if (!title) return alert("Title cannot be empty.");

    const payload = {
      title,
      content: editorRef.current.innerHTML.trim(),
      tags,
    };

    try {
      const url = isEditing
        ? `${apiurl}/api/notes/${note.id}`
        : `${apiurl}/api/notes`;
      const method = isEditing ? axios.put : axios.post;
      const res = await method(url, payload, { headers });
      onSave(res.data, !isEditing);
    } catch (err) {
      console.error("Save failed", err);
      alert("Save failed");
    }
  };

  useEffect(() => {
    const handleCheckboxChange = (event) => {
      const checkbox = event.target;
      if (checkbox.type === "checkbox") {
        checkbox.checked
          ? checkbox.setAttribute("checked", "checked")
          : checkbox.removeAttribute("checked");
      }
    };

    const editorElement = editorRef.current;
    if (editorElement) {
      editorElement.addEventListener("change", handleCheckboxChange, true);
    }

    return () => {
      if (editorElement) {
        editorElement.removeEventListener("change", handleCheckboxChange, true);
      }
    };
  }, []);

  const openShareBox = async () => {
    try {
      const res = await axios.get(
        `${apiurl}/api/notes/SharedUsers/${note.id}`,
        { headers }
      );
      setSharedUsers(res.data);
      setShowShareBox(true);
    } catch (err) {
      console.error("Failed to fetch shared users", err);
      //alert('Failed to open share box');
    }
  };

  const closeShareBox = () => {
    setShowShareBox(false);
    setSearchQuery("");
    setSearchResults([]);
  };

  const searchUsers = async (query) => {
    setSearchQuery(query);
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }
    try {
      const res = await axios.get(
        `${apiurl}/api/users/search?query=${query}`,
        { headers }
      );
      setSearchResults(res.data);
    } catch (err) {
      console.error("Search failed", err);
    }
  };

  const shareWithUser = async (userId) => {
    try {
      await axios.get(
        `${apiurl}/api/notes/share/${note.id}/${userId}`,
        { headers }
      );
      const addedUser = searchResults.find((u) => u.id === userId);
      if (addedUser) {
        setSharedUsers((prev) => [...prev, addedUser]);
      }
      setSearchQuery("");
      setSearchResults([]);
    } catch (err) {
      console.error("Share failed", err);
    }
  };

  const revokeShare = async (userId) => {
    try {
      await axios.get(
        `${apiurl}/api/notes/revoke/${note.id}/${userId}`,
        { headers }
      );
      setSharedUsers((prev) => prev.filter((u) => u.id !== userId));
    } catch (err) {
      console.error("Unshare failed", err);
    }
  };

  return (
    <div className="p-4 rounded-xl border border-gray-300 shadow-md bg-white mx-auto relative w-full max-w-3xl sm:p-6">
      {/* Title and Tag Section */}
      <div className="flex sm:flex-row items-start sm:items-center flex-wrap justify-between gap-2 items-center px-4 pt-4 pb-2">
        <div
          ref={titleRef}
          contentEditable
          suppressContentEditableWarning
          onInput={() => setPh(titleRef, "Title")}
          className="text-lg font-medium outline-none min-h-[24px] relative flex-1 truncate
            before:content-[attr(data-placeholder)] before:absolute before:left-4 before:top-1
            before:text-gray-400 before:pointer-events-none empty:before:block overflow-hidden whitespace-nowrap"
          data-placeholder="Title"
        />
        <div className="flex items-center gap-1">
          <FontAwesomeIcon icon={faTag} className="text-gray-400" />
          <input
            type="text"
            value={newTag}
            placeholder="Add tag"
            onChange={(e) => setNewTag(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && addTag()}
            className="border-b text-sm focus:outline-none border-gray-200 focus:border-gray-400 py-1 px-1 w-full sm:w-32"
          />
          <button
            onClick={addTag}
            className="text-sm text-blue-500 hover:underline"
          >
            Add
          </button>
        </div>
      </div>

      {/* Tags List */}
      <div className="px-4 flex flex-wrap gap-2 mt-1 border-b border-gray-200 pb-2">
        {tags.map((tag) => (
          <span
            key={tag}
            className="bg-gray-100 text-sm px-2 py-1 rounded-full flex items-center gap-1 "
          >
            <FontAwesomeIcon icon={faTag} className="text-gray-400" />
            {tag}
            <button
              onClick={() => removeTag(tag)}
              className="text-gray-400 hover:text-gray-600"
            >
              <FontAwesomeIcon icon={faTimes} size="xs" />
            </button>
          </span>
        ))}
      </div>

      {/* Note body */}
      <div
        ref={editorRef}
        contentEditable
        suppressContentEditableWarning
        onInput={() => setPh(editorRef, "Take a note…")}
        className="min-h-[100px] px-4 py-2 outline-none text-gray-800 relative
          before:content-[attr(data-placeholder)] before:absolute before:left-4 before:top-2
          before:text-gray-400 before:pointer-events-none empty:before:block"
        data-placeholder="Take a note…"
      />

      {/* Toolbar */}
      <div className="flex justify-between items-center border-t border-gray-200 px-3 pt-1 sm:flex-row sm:justify-between gap-2 items-start sm:items-center">
        <div className="flex gap-2 text-gray-500 flex-wrap">
          <button
            onClick={() => exec("bold")}
            className="hover:bg-gray-100 p-2 rounded-full"
          >
            <FontAwesomeIcon icon={faBold} />
          </button>
          <button
            onClick={() => exec("italic")}
            className="hover:bg-gray-100 p-2 rounded-full"
          >
            <FontAwesomeIcon icon={faItalic} />
          </button>
          <button
            onClick={() => exec("underline")}
            className="hover:bg-gray-100 p-2 rounded-full"
          >
            <FontAwesomeIcon icon={faUnderline} />
          </button>
          <button
            onClick={insertCheckbox}
            className="hover:bg-gray-100 p-2 rounded-full"
          >
            <FontAwesomeIcon icon={faCheckSquare} />
          </button>
          <button
            onClick={() => fileInput.current.click()}
            className="hover:bg-gray-100 p-2 rounded-full"
          >
            <FontAwesomeIcon icon={faImage} />
          </button>
          <button
            onClick={openShareBox}
            className="hover:bg-gray-100 p-2 rounded-full flex items-center gap-1"
          >
            <FontAwesomeIcon icon={faShareFromSquare} />
            Share
          </button>
          <input
            ref={fileInput}
            type="file"
            accept="image/*"
            onChange={insertImage}
            className="hidden"
          />
        </div>
        <div className="flex gap-2">
          <button
            onClick={saveNote}
            className="flex items-center gap-1 px-4 py-2 text-gray-500 hover:bg-gray-100 rounded-full"
          >
            <FontAwesomeIcon icon={faSave} /> Save
          </button>
          <button
            onClick={onClose}
            className="text-gray-500 hover:bg-gray-100 px-4 py-2 rounded-full"
          >
            Close
          </button>
        </div>
      </div>

      {/* Share Box */}
      {showShareBox && (
        <div className="absolute top-20 right-10 left-4 z-50  sm:left-auto sm:right-10 w-full sm:w-96 bg-white  rounded-2xl shadow-xl border border-gray-200 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium text-gray-800">Share Note</h3>
          </div>

          {sharedUsers.length > 0 && (
            <div className="space-y-2">
              <ul className="space-y-1">
                {sharedUsers.map((user) => (
                  <li
                    key={user.id}
                    className="flex items-center justify-between bg-gray-50 px-3 py-2 rounded-lg border border-gray-200 text-sm text-gray-700"
                  >
                    <div className="flex items-center space-x-2">
                      <FontAwesomeIcon
                        icon={faUser}
                        className="text-gray-500"
                      />
                      <span>{user.username}</span>
                    </div>
                    <button
                      onClick={() => revokeShare(user.id)}
                      className="p-1 hover:scale-120 rounded-full transition-transform transform"
                      title="Remove collaborator"
                    >
                      <FontAwesomeIcon
                        icon={faClose}
                        className="text-gray-500"
                      />
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => searchUsers(e.target.value)}
              placeholder="Search users to add"
              className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
            />
            {searchResults.length > 0 && (
              <ul className="mt-2 max-h-40 overflow-y-auto border border-gray-200 rounded-lg shadow-sm bg-white divide-y divide-gray-100">
                {searchResults.map((user) => (
                  <li
                    key={user.id}
                    className="px-3 py-2 hover:bg-gray-100 text-sm cursor-pointer text-gray-700 transition"
                    onClick={() => shareWithUser(user.id)}
                  >
                    {user.username}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="text-right">
            <button
              onClick={closeShareBox}
              className="mt-2 text-sm text-gray-600 px-4 py-1 rounded-lg hover:bg-gray-100 transition"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default NoteEditor;
