import { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faBold, faItalic, faUnderline, faCheckSquare,
  faImage, faSave, faTag, faTimes
} from '@fortawesome/free-solid-svg-icons';

const NoteEditor = ({ note, onSave, onClose }) => {
  const isEditing = Boolean(note?.id);
  const editorRef = useRef(null);
  const titleRef = useRef(null);
  const fileInput = useRef(null);

  const [tags, setTags] = useState([]);
  const [newTag, setNewTag] = useState('');

  const token = localStorage.getItem('token');
  const headers = { Authorization: `Bearer ${token}` };

  const setPh = (ref, ph) => {
    if (ref.current.innerText.trim() === '') {
      ref.current.innerHTML = '&#8203;';
      ref.current.dataset.placeholder = ph;
    } else ref.current.dataset.placeholder = '';
  };

  const exec = cmd => document.execCommand(cmd, false, null);

  const insertCheckbox = () => {
    const cb = document.createElement('input');
    cb.type = 'checkbox';
    editorRef.current.append(cb, ' ');
  };

  const insertImage = e => {
    const f = e.target.files[0];
    if (!f) return;
    const r = new FileReader();
    r.onload = ev => {
      const img = document.createElement('img');
      img.src = ev.target.result;
      img.style.maxWidth = '100%';
      editorRef.current.append(img);
      fileInput.current.value = '';
    };
    r.readAsDataURL(f);
  };

  useEffect(() => {
    if (isEditing && note) {
      titleRef.current.innerText = note.title || '';
      editorRef.current.innerHTML = note.content || '';
      setTags(note.tags || []);
    } else {
      titleRef.current.innerText = '';
      editorRef.current.innerHTML = '';
      setTags([]);
    }
    setPh(titleRef, 'Title');
    setPh(editorRef, 'Take a note…');
  }, [note, isEditing]);

  const addTag = () => {
    const t = newTag.trim();
    if (t && !tags.includes(t)) setTags([...tags, t]);
    setNewTag('');
  };

  const removeTag = tagToRemove =>
    setTags(tags.filter(t => t !== tagToRemove));

  const saveNote = async () => {
    const title = titleRef.current.innerText.trim().replace(/\u200B/g, '');
    if (!title) return alert('Title cannot be empty.');

    const payload = {
      title,
      content: editorRef.current.innerHTML.trim(),
      tags
    };

    try {
      const url = isEditing
        ? `http://localhost:8080/api/notes/${note.id}`
        : `http://localhost:8080/api/notes`;
      const method = isEditing ? axios.put : axios.post;
      const res = await method(url, payload, { headers });
      onSave(res.data, !isEditing);
    } catch (err) {
      console.error('Save failed', err);
      alert('Save failed');
    }
  };

  // Add event listener for checkbox state changes using event delegation
  useEffect(() => {
    const handleCheckboxChange = (event) => {
      const checkbox = event.target;
      if (checkbox.type === 'checkbox') {
        const isChecked = checkbox.checked;
        if (isChecked) {
          checkbox.setAttribute('checked', 'checked');
        } else {
          checkbox.removeAttribute('checked');
        }
      }
    };

    // Attach event listener to the editor's parent
    const editorElement = editorRef.current;
    if (editorElement) {
      editorElement.addEventListener('change', handleCheckboxChange, true);
    }

    // Clean up the event listener on component unmount
    return () => {
      if (editorElement) {
        editorElement.removeEventListener('change', handleCheckboxChange, true);
      }
    };
  }, []);

  return (
    <div className="p-4 rounded-xl border border-gray-300 shadow-md bg-white mx-auto">
      {/* Title and Tag Section */}
      <div className="flex flex-wrap justify-between gap-2 items-center px-4 pt-4 pb-2">
        <div
          ref={titleRef}
          contentEditable
          suppressContentEditableWarning
          onInput={() => setPh(titleRef, 'Title')}
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
            onChange={e => setNewTag(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && addTag()}
            className="border-b text-sm focus:outline-none border-gray-200 focus:border-gray-400 py-1 px-1 w-28"
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
        {tags.map(tag => (
          <span key={tag} className="bg-gray-100 text-sm px-2 py-1 rounded-full flex items-center gap-1 ">
            <FontAwesomeIcon icon={faTag} className="text-gray-400" />
            {tag}
            <button onClick={() => removeTag(tag)} className="text-gray-400 hover:text-gray-600">
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
        onInput={() => setPh(editorRef, 'Take a note…')}
        className="min-h-[100px] px-4 py-2 outline-none text-gray-800 relative
                   before:content-[attr(data-placeholder)] before:absolute before:left-4 before:top-2
                   before:text-gray-400 before:pointer-events-none empty:before:block"
        data-placeholder="Take a note…"
      />

      {/* Toolbar */}
      <div className="flex justify-between items-center border-t border-gray-200 px-3">
        <div className="flex gap-2 text-gray-500">
          <button onClick={() => exec('bold')} className="hover:bg-gray-100 p-2 rounded-full"><FontAwesomeIcon icon={faBold} /></button>
          <button onClick={() => exec('italic')} className="hover:bg-gray-100 p-2 rounded-full"><FontAwesomeIcon icon={faItalic} /></button>
          <button onClick={() => exec('underline')} className="hover:bg-gray-100 p-2 rounded-full"><FontAwesomeIcon icon={faUnderline} /></button>
          <button onClick={insertCheckbox} className="hover:bg-gray-100 p-2 rounded-full"><FontAwesomeIcon icon={faCheckSquare} /></button>
          <button onClick={() => fileInput.current.click()} className="hover:bg-gray-100 p-2 rounded-full"><FontAwesomeIcon icon={faImage} /></button>
          <input ref={fileInput} type="file" accept="image/*" onChange={insertImage} className="hidden" />
        </div>

        <div className="flex gap-2">
          <button onClick={saveNote} className="flex items-center gap-1 px-4 py-2 text-gray-500 hover:bg-gray-100 rounded-full">
            <FontAwesomeIcon icon={faSave} /> Save
          </button>
          <button onClick={onClose} className="text-gray-500 hover:bg-gray-100 px-4 py-2 rounded-full">Close</button>
        </div>
      </div>
    </div>
  );
};

export default NoteEditor;