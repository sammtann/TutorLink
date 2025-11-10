import { useState } from "react";
import { XMarkIcon } from "@heroicons/react/24/outline";

interface LessonTypeInputProps {
  value: string[];
  onChange: (newValue: string[]) => void;
  suggestions?: string[];
}

const TutorLessonTypeInput: React.FC<LessonTypeInputProps> = ({
  value = [],
  onChange,
  suggestions = [],
}) => {
  const [input, setInput] = useState("");
  const [showSuggestions, setShowSuggestions] = useState(false);

  const addLesson = (lesson: string) => {
    const trimmed = lesson.trim();
    if (!trimmed || value.includes(trimmed)) return;
    onChange([...value, trimmed]);
    setInput("");
    setShowSuggestions(false);
  };

  const removeLesson = (lesson: string) => {
    onChange(value.filter((l) => l !== lesson));
  };

  const filteredSuggestions = suggestions.filter(
    (s) => s.toLowerCase().includes(input.toLowerCase()) && !value.includes(s)
  );

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && input.trim()) {
      e.preventDefault();
      addLesson(input);
    }
  };

  return (
    <div className="space-y-2 relative">
      <label className="block text-sm font-medium">Lesson Types</label>
      <div className="flex flex-wrap gap-2 p-2 border rounded-lg min-h-[3rem] bg-gray-50">
        {value.map((lesson) => (
          <span
            key={lesson}
            className="flex items-center bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm">
            {lesson}
            <button
              onClick={() => removeLesson(lesson)}
              className="ml-2 hover:text-red-600"
              type="button">
              <XMarkIcon className="h-4 w-4" />
            </button>
          </span>
        ))}
        <input
          className="flex-1 bg-transparent outline-none text-sm"
          type="text"
          required
          placeholder="Type or select..."
          value={input}
          onChange={(e) => {
            setInput(e.target.value);
            setShowSuggestions(true);
          }}
          onKeyDown={handleKeyDown}
        />
      </div>

      {showSuggestions && filteredSuggestions.length > 0 && (
        <ul className="absolute z-10 bg-white border rounded-lg shadow-md mt-1 max-h-40 overflow-y-auto w-full">
          {filteredSuggestions.map((s) => (
            <li
              key={s}
              className="px-3 py-2 text-sm hover:bg-blue-100 cursor-pointer"
              onClick={() => addLesson(s)}>
              {s}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default TutorLessonTypeInput;
