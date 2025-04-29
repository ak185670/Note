import { useCallback, useState,useEffect} from "react"
import axios from "axios";
import NoteEditor from "./NoteEditor";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faThumbtack, faThumbtackSlash, faArchive, faBoxArchive,
  faTrash, faTag
} from "@fortawesome/free-solid-svg-icons";


const ArchivedNotes = () => {

    const token = localStorage.getItem("token");
    const headers = { Authorization: `Bearer ${token}` };
    const [archivedNotes,setArchivedNotes]=useState([]);
    const [activeNote, setActiveNote] = useState(null);
    

    const loadNotes = useCallback(async () => {
        try {
          const { data } = await axios.get("http://localhost:8080/api/notes/archived", {
            headers
          });
          setArchivedNotes(data);
        } catch (_) {}
      }, []);

    const handleSave = () => { setActiveNote(null); loadNotes(); };

    useEffect(() => {
        loadNotes();
        }, [loadNotes]);

    const toggleArchive = note => axios.post(`http://localhost:8080/api/notes/toggleArchive/${note.id}`, null, { headers }).then(loadNotes);
    const deleteNote = id => axios.delete(`http://localhost:8080/api/notes/delete/${id}`, { headers }).then(loadNotes);
  return (
    <div className="relative p-4 max-w-6xl mx-auto">
        <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {archivedNotes.map(note => (
            <div
                key={note.id}
                className="relative border border-gray-300 shadow-md bg-white p-4 pb-2 rounded-lg hover:bg-gray-50 transition group"
                onClick={() => setActiveNote(note)}
            >
                <div className="absolute top-2 right-2 flex gap-1" onClick={e => e.stopPropagation()}>
                
                <button title={note.archived ? "Unarchive" : "Archive"} onClick={() => toggleArchive(note)} className="p-1 hover:text-black text-gray-500 rounded-full active:scale-95 transition">
                    <FontAwesomeIcon icon={note.archived ? faBoxArchive : faArchive} className="h-4 w-4" />
                </button>
                <button title="Delete" onClick={() => deleteNote(note.id)} className="p-1 rounded-full hover:text-black active:scale-95 text-gray-500 transition">
                    <FontAwesomeIcon icon={faTrash} className="h-4 w-4" />
                </button>
                </div>

                <h2 className="text-xl font-semibold mb-2 text-gray-500">
                {note.title || "Untitled"}
                </h2>
                <div dangerouslySetInnerHTML={{ __html: note.content }} className="preview-area text-sm text-gray-600 line-clamp-3" />
                {note.tags?.length > 0 && (
                <div className="pt-3 text-sm mb-2 text-gray-500 font-semibold flex flex-wrap gap-2 items-center">
                    {note.tags.map((tag, index) => (
                    <span key={index} className="bg-gray-200 text-gray-700 px-2 py-0.5 rounded-full text-xs">
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
    
  )
}

export default ArchivedNotes;
