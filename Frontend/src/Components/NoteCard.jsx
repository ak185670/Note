import React from 'react';
import { useNavigate } from 'react-router-dom';

const NoteCard = ({ note }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/note/${note.id}`);
  };

  return (
    <div
      onClick={handleClick}
      className="bg-white shadow-md rounded-2xl p-4 hover:shadow-lg transition cursor-pointer"
    >
      <h2 className="text-xl font-semibold mb-2">{note.title}</h2>

      <div
        className="text-sm text-gray-700 line-clamp-3 mb-3"
        dangerouslySetInnerHTML={{ __html: note.content }}
      />

      <div className="flex items-center justify-between">
        <div className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-md">
          {note.tag}
        </div>

        <div className="flex gap-2">
          <button className="text-yellow-500 hover:text-yellow-600">
            {note.pinned ? 'Unpin' : 'Pin'}
          </button>
          <button className="text-gray-500 hover:text-gray-600">
            {note.archived ? 'Unarchive' : 'Archive'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default NoteCard;