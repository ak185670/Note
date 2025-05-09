import { useEffect, useState, useCallback, useRef } from "react";
import axios from "axios";
import NoteEditor from "./NoteEditor";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faThumbtack, faThumbtackSlash, faArchive, faBoxArchive,
  faTrash, faTag, faSearch, faSort, faCircleCheck
} from "@fortawesome/free-solid-svg-icons";


const Dashboard = () => {
  const apiurl =import.meta.env.VITE_API_URL;
  const [notes, setNotes] = useState([]);
  const [activeNote, setActiveNote] = useState(null);
  const [creatingNew, setCreatingNew] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortOption, setSortOption] = useState("");
  const [selectedTag, setSelectedTag] = useState("");
  const [showArchived, setShowArchived] = useState(false);
  const [showFilterDropdown, setShowFilterDropdown] = useState(false);
  const[username,setUsername]= useState("");

  const filterRef = useRef(null);

  const token = localStorage.getItem("token");
  const headers = { Authorization: `Bearer ${token}` };

  const getUsername = useCallback(async()=> {

      const {data} =await axios.get(`${apiurl}/api/users`,{headers});
      setUsername(data);
      
  },[])


  const loadNotes = useCallback(async () => {
    try {
      
      const { data } = await axios.get(`${apiurl}/api/notes`, {
        headers,
        params: { search: searchTerm, sortBy: sortOption, tag: selectedTag, archived: showArchived },
      });
      setNotes(data);
      getUsername()

      console.log(data);
    } catch (_) {}
  }, [searchTerm, sortOption, selectedTag, showArchived]);

  useEffect(() => { loadNotes();  }, [loadNotes]);

  const togglePin = note => axios.post(`${apiurl}/api/notes/togglePin/${note.id}`, null, { headers }).then(loadNotes);
  const toggleArchive = note => axios.post(`${apiurl}/api/notes/toggleArchive/${note.id}`, null, { headers }).then(loadNotes);
  const deleteNote = id => axios.delete(`${apiurl}/api/notes/delete/${id}`, { headers }).then(loadNotes);

  const handleSave = () => { setActiveNote(null); setCreatingNew(false); loadNotes(); };

  const handleSearchChange = (e) => setSearchTerm(e.target.value);
  const handleSearchSubmit = (e) => { e.preventDefault(); loadNotes(); };

  const handleSortClick = (option) => {
    setSortOption(prev => (prev === option ? "" : option));
  };

  const handleTagClick = (tag) => {
    setSelectedTag(prev => (prev === tag ? "" : tag));
  };

  const toggleArchived = () => {
    setShowArchived(prev => !prev);
  };
  
  const handleDelete=(id)=>{
    if(confirm("Delete?"))
    {
      deleteNote(id);
    }
  }

  const allTags = Array.from(new Set(notes.flatMap(note => note.tags || [])));

  // Close filter when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (filterRef.current && !filterRef.current.contains(event.target)) {
        setShowFilterDropdown(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => { document.removeEventListener("mousedown", handleClickOutside); };
  }, []);

  return (
    <div className="relative p-4 max-w-7xl mx-auto">
      
      {/* Create New Note */}
      <div className="mb-6 mt-4">
        {creatingNew ? (
          <NoteEditor onSave={handleSave} onClose={() => setCreatingNew(false)} />
        ) : (
          <div
            className="bg-white border border-gray-300 shadow-md text-gray-500 p-4 rounded-lg cursor-pointer hover:bg-gray-100 transition"
            onClick={() => setCreatingNew(true)}
          >
            + Take a note...
          </div>
        )}
      </div>

      {/* Search and Filter */}
      <div className="flex items-center justify-between mb-6 gap-4">
        
        {/* Search */}
        <form onSubmit={handleSearchSubmit} className="flex max-w-md w-full items-center border border-gray-300 rounded-lg p-2 bg-white">
          <FontAwesomeIcon icon={faSearch} className="pl-2 text-gray-600" />
          <input
            type="text"
            value={searchTerm}
            onChange={handleSearchChange}
            placeholder="Search Notes..."
            className="flex-1 p-2 outline-none text-gray-700 bg-transparent"
          />
        </form>

        {/* Filter */}
        <div className="relative" ref={filterRef}>
          <button
            onClick={() => setShowFilterDropdown(prev => !prev)}
            className="border border-gray-300 px-6 py-3 max-w-md w-full rounded-lg text-gray-600 hover:bg-gray-100 bg-white flex items-center gap-2"
          >
            <FontAwesomeIcon icon={faSort} />
            Filters
          </button>

          {showFilterDropdown && (
            <div className="absolute right-0 z-50 mt-2 w-64 bg-white shadow-lg border border-gray-300 rounded-lg p-4 space-y-4">

              {/* Sort Options */}
              <div className="flex flex-col gap-2">
                <button
                  onClick={() => handleSortClick("createdAt")}
                  className={`flex items-center gap-2 p-2 rounded-md ${sortOption === "createdAt" ? "bg-gray-200 text-gray-800 font-semibold" : "hover:bg-gray-100"}`}
                >
                  <FontAwesomeIcon icon={faCircleCheck} className={`${sortOption === "createdAt" ? "text-gray-700" : "text-gray-400"}`} />
                  Sort By Creation Date
                </button>

                <button
                  onClick={() => handleSortClick("lastEdited")}
                  className={`flex items-center gap-2 p-2 rounded-md ${sortOption === "lastEdited" ? "bg-gray-200 text-gray-800 font-semibold" : "hover:bg-gray-100"}`}
                >
                  <FontAwesomeIcon icon={faCircleCheck} className={`${sortOption === "lastEdited" ? "text-gray-700" : "text-gray-400"}`} />
                  Recently Edited
                </button>
              </div>

              {/* Tag Options */}
              <div className="flex flex-wrap gap-2 max-h-32 overflow-y-auto">
                {allTags.map((tag, index) => (
                  <button
                    key={index}
                    onClick={() => handleTagClick(tag)}
                    className={`px-3 py-1 rounded-full text-xs flex items-center gap-1 ${
                      selectedTag === tag ? "bg-gray-300 text-gray-800 font-semibold" : "bg-gray-100 hover:bg-gray-200"
                    }`}
                  >
                    <FontAwesomeIcon icon={faTag} className="text-gray-500" />
                    {tag}
                  </button>
                ))}
              </div>

              {/* Archived Toggle */}
              <div>
                <button
                  onClick={toggleArchived}
                  className={`flex items-center gap-2 p-2 rounded-md w-full ${
                    showArchived ? "bg-gray-200 text-gray-800 font-semibold" : "hover:bg-gray-100"
                  }`}
                >
                  <FontAwesomeIcon icon={showArchived ? faBoxArchive : faArchive} className="text-gray-500" />
                  {showArchived ? "Showing Archived" : "Show Archived"}
                </button>
              </div>

            </div>
          )}
        </div>

      </div>

      {/* Notes Grid */}
      <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {notes.map(note => (
          <div
            key={note.id}
            className="relative border border-gray-300 shadow-md bg-white p-4 pb-2 rounded-lg hover:bg-gray-50 transition group"
            onClick={() => setActiveNote(note)}
          >
            {note.shared && (
              <div className="absolute top-0 left-0 bg-black text-white text-[10px] font-bold px-2 py-1 rounded-br-lg z-10">
                SHARED
              </div>
            )}
            <div className="absolute top-2 right-2 flex gap-1" onClick={e => e.stopPropagation()}>
              <button title={note.pinned ? "Unpin" : "Pin"} onClick={() => togglePin(note)} className="p-1 text-gray-500 hover:text-black rounded-full active:scale-95 transition">
                <FontAwesomeIcon icon={note.pinned ? faThumbtackSlash : faThumbtack} className="h-5 w-5" />
              </button>
              <button title={note.archived ? "Unarchive" : "Archive"} onClick={() => toggleArchive(note)} className="p-1 hover:text-black text-gray-500 rounded-full active:scale-95 transition">
                <FontAwesomeIcon icon={note.archived ? faBoxArchive : faArchive} className="h-4 w-4" />
              </button>
              <button title={note.createdUser=== username ? "Delete":"Delete For You"} onClick={() => handleDelete(note.id)} className="p-1 rounded-full hover:text-black active:scale-95 text-gray-500 transition">
                <FontAwesomeIcon icon={faTrash} className="h-4 w-4" />
              </button>
            </div>

            <h2 className={`text-xl font-semibold mb-2 text-gray-700 ${note.shared?'pt-3':''} `}>
              {note.title || "Untitled"}
            </h2>
            <div dangerouslySetInnerHTML={{ __html: note.content }} className="preview-area text-sm text-gray-600 line-clamp-3" />
            {note.tags?.length > 0 && (
              <div className="mt-1 pt-3 text-sm mb-2 text-gray-500 font-semibold flex flex-wrap gap-2 items-center">
                {note.tags.map((tag, index) => (
                  <span key={index} className=" bg-gray-100 text-gray-700 px-2 px-1.5 py-1 rounded-full text-xs">
                    <FontAwesomeIcon icon={faTag} className="text-gray-500 pr-1" />
                    {tag}
                  </span>
                ))}
              </div>
            )}
            
          </div>
        ))}
      </div>

      {/* Modal Editor */}
      {activeNote && (
        <div className="fixed inset-0 z-40 flex items-start justify-center bg-black/40 p-20 overflow-y-auto">
          <div className="w-full max-w-3xl">
            <NoteEditor note={activeNote} onSave={handleSave} onClose={() => setActiveNote(null)} />
          </div>
        </div>
      )}

    </div>
  );
};

export default Dashboard;