import React, { useState } from "react";

export interface BookingCardProps {
  id: string;
  tutorId?: string;
  studentId?: string;
  tutorName: string;
  studentName: string;
  date: string;
  start: string;
  end: string;
  status: "confirmed" | "pending" | "cancelled";
  lessonType: string;
  isPastSession?: boolean;
  isDashboard?: boolean; // ✅ new prop
  onClick?: (id: string) => void;
  onCancel?: (id: string) => void;
  onReschedule?: (bookingId: string, tutorId: string, tutorName: string, studentName: string) => void;
  onReview?: (bookingId: string) => void;
}

const BookingCard: React.FC<BookingCardProps> = ({
  id,
  tutorId,
  tutorName,
  studentName,
  date,
  start,
  end,
  lessonType,
  status,
  isPastSession = false,
  isDashboard = false, // ✅ default false
  onClick,
  onCancel,
  onReschedule,
  onReview,
}) => {
  const [isHovered, setIsHovered] = useState(false);

  const statusColor = {
    confirmed: "bg-green-100 text-green-800",
    pending: "bg-yellow-100 text-yellow-800",
    cancelled: "bg-red-100 text-red-800",
    on_hold: "bg-orange-100 text-orange-800",
    reschedule_requested: "bg-purple-100 text-purple-800",
  } as const;

  const handleCancelClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onCancel) onCancel(id);
  };

  // Disable hover overlays for past sessions
  const showCancelOverlay = !isPastSession && status === "pending" && isHovered;
  const showRescheduleOverlay = !isPastSession && status === "confirmed" && isHovered;

  const month = new Date(date).toLocaleString("default", { month: "short" });
  const day = new Date(date).getDate();
  const weekday = new Date(date).toLocaleDateString(undefined, {
    weekday: "short",
    year: "numeric",
  });

  return (
    <div
      onClick={() => onClick && onClick(id)}
      onMouseEnter={() => !isPastSession && setIsHovered(true)}
      onMouseLeave={() => !isPastSession && setIsHovered(false)}
      className={`relative flex flex-col border rounded-md p-4 shadow-sm transition ${showCancelOverlay ? "bg-red-100" : "bg-white hover:shadow-md"
        } cursor-pointer`}>
      <div className="flex items-center justify-between">
        {/* Left: Date */}
        <div className="text-center pr-4 border-r">
          <p className="uppercase text-xs text-gray-500">{month}</p>
          <p className="font-bold text-xl">{day}</p>
          <p className="text-xs text-gray-500">{weekday}</p>
        </div>

        {/* Middle: Info */}
        <div className="flex-1 px-4">
          <p className="font-semibold text-base">{lessonType}</p>
          <p className="text-sm text-gray-600">
            {start} - {end} 
          </p>
          <p className="text-sm text-gray-600">{tutorName}</p>
        </div>

        {/* Right: Status or Review */}
        {!isDashboard && (
          <div className="flex flex-col items-end">
            {isPastSession ? (
              status === "confirmed" ? (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    if (onReview) onReview(id);
                  }}
                  className="mt-2 px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">
                  Review
                </button>
              ) : (
                <span className="text-sm text-gray-400 mt-2">N/A</span>
              )
            ) : (
              <span
                className={`px-2 py-1 rounded-md text-sm font-semibold ${statusColor[status]}`}
              >
                {status
                  .split('_') 
                  .map(word => word.charAt(0).toUpperCase() + word.slice(1)) 
                  .join(' ')} 
              </span>
            )}
          </div>
        )}
      </div>

      {/* Cancel overlay for pending */}
      {showCancelOverlay && (
        <button
          onClick={handleCancelClick}
          className="absolute inset-0 flex items-center justify-center bg-red-500/70 text-white font-bold rounded-md backdrop-blur-sm"
          aria-label="Cancel Booking">
          Cancel
        </button>
      )}

      {/* Reschedule overlay for confirmed */}
      {showRescheduleOverlay && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            if (onReschedule && tutorId) onReschedule(id, tutorId, tutorName, studentName);
          }}
          className="absolute inset-0 flex items-center justify-center bg-blue-500/70 text-white font-bold rounded-md backdrop-blur-sm opacity-0 hover:opacity-100 transition"
          aria-label="Reschedule Booking">
          Reschedule
        </button>
      )}
    </div>
  );
};

export default BookingCard;
