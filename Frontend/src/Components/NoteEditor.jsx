import React, { useRef, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCheckSquare, faImage, faSave, faBold, faItalic, faUnderline } from '@fortawesome/free-solid-svg-icons';
import '../index.css';


const NoteEditor = () => {
    const editorRef = useRef(null);
    const fileInputRef = useRef(null);
    const titleRef = useRef(null);
    const [titlePlaceholder, setTitlePlaceholder] = useState('Title...');
    const [fontSize, setFontSize] = useState('16px');
    const [fontColor, setFontColor] = useState('#000000');

    const insertCheckbox = () => {
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        editorRef.current.appendChild(checkbox);
        editorRef.current.appendChild(document.createTextNode(' '));
    };

    const insertImage = (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.style.maxWidth = '100%';
                editorRef.current.appendChild(img);
                fileInputRef.current.value = '';
            };
            reader.readAsDataURL(file);
        }
    };

    const triggerFileInput = () => {
        fileInputRef.current.click();
    };

    const handleTitleFocus = () => {
        if (titleRef.current.innerText === 'Title...') {
            titleRef.current.innerText = '';
        }
    };

    const handleTitleBlur = () => {
        if (titleRef.current.innerText.trim() === '') {
            titleRef.current.innerText = 'Title...';
        }
    };

    const saveContent = async () => {
        const title = titleRef.current.innerText;
        const content = editorRef.current.innerHTML;
        const note = { title, content };
        try {
            await axios.post('http://localhost:5000/api/notes', note);
            alert('Note saved!');
            fetchNotes(); 
        } catch (error) {
            console.error('Error saving note:', error);
            alert('Failed to save note.');
        }
    };

    const applyStyle = (command, value = null) => {
        document.execCommand(command, false, value);
    };

    const handleFontSizeChange = (event) => {
        setFontSize(event.target.value);
        applyStyle('fontSize', event.target.value);
    };

    const handleFontColorChange = (event) => {
        setFontColor(event.target.value);
        applyStyle('foreColor', event.target.value);
    };

    return (
        <div className="max-w-4xl mx-auto mt-10">
            <div
                ref={titleRef}
                contentEditable="true"
                className="bg-black text-white text-2xl p-2 min-h-[50px] border border-gray-400 rounded-t-lg"
                onFocus={handleTitleFocus}
                onBlur={handleTitleBlur}
            >
                {titlePlaceholder}
            </div>

            <div className="bg-gray-800 p-2 flex gap-2 flex-wrap">
                <button
                    onClick={insertCheckbox}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                >
                    <FontAwesomeIcon icon={faCheckSquare} />
                </button>
                <button
                    onClick={triggerFileInput}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                >
                    <FontAwesomeIcon icon={faImage} />
                </button>
                <input
                    type="file"
                    ref={fileInputRef}
                    style={{ display: 'none' }}
                    onChange={insertImage}
                    accept="image/*"
                />
                <button
                    onClick={() => applyStyle('bold')}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                >
                    <FontAwesomeIcon icon={faBold} />
                </button>
                <button
                    onClick={() => applyStyle('italic')}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                >
                    <FontAwesomeIcon icon={faItalic} />
                </button>
                <button
                    onClick={() => applyStyle('underline')}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                >
                    <FontAwesomeIcon icon={faUnderline} />
                </button>
                <select
                    value={fontSize}
                    onChange={handleFontSizeChange}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                >
                    <option value="1">8px</option>
                    <option value="2">10px</option>
                    <option value="3">12px</option>
                    <option value="4">14px</option>
                    <option value="5">16px</option>
                    <option value="6">18px</option>
                    <option value="7">20px</option>
                </select>
                <input
                    type="color"
                    value={fontColor}
                    onChange={handleFontColorChange}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                />
               
                <button
                    onClick={saveContent}
                    className="bg-black text-white px-3 py-1 rounded flex items-center gap-2"
                >
                    <FontAwesomeIcon icon={faSave} />
                    Save
                </button>
            </div>

            <div
                ref={editorRef}
                contentEditable="true"
                className="bg-black text-white p-4 min-h-[300px] border border-gray-400 rounded-b-lg"
            ></div>
        </div>
    );
};

export default NoteEditor;
