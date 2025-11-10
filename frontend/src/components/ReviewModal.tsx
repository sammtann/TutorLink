import React, { useState } from "react";
import { AddTutorReview } from "@/api/tutorAPI";
import { toast } from "react-toastify";

interface ReviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  tutorId: string;
  bookingId: string;
  studentName: string;
  token: string;
}

const ReviewModal: React.FC<ReviewModalProps> = ({
  isOpen,
  onClose,
  tutorId,
  bookingId,
  studentName,
  token,
}) => {
  const [rating, setRating] = useState<number>(0);
  const [comment, setComment] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);

  if (!isOpen) return null;

  const handleSubmit = async () => {
    if (rating === 0) {
      toast.warning("Please provide a rating before submitting");
      return;
    }

    try {
      setLoading(true);
      await AddTutorReview(token, tutorId, {
        bookingId,
        studentName,
        rating,
        comment,
      });
      toast.success("Review submitted successfully!");
      onClose();
      setRating(0);
      setComment("");
    } catch (err: any) {
      console.error("Error submitting review:", err);
      if (err.response?.data?.message?.includes("already reviewed")) {
        toast.warning("You’ve already submitted a review for this session.");
      } else {
        toast.error("Failed to submit review. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6">
        <h2 className="text-xl font-bold mb-4 text-center">Leave a Review</h2>

        {/* Rating stars */}
        <div className="flex justify-center mb-4">
          {[1, 2, 3, 4, 5].map((star) => (
            <span
              key={star}
              onClick={() => setRating(star)}
              className={`text-3xl cursor-pointer ${
                star <= rating ? "text-yellow-400" : "text-gray-300"
              }`}>
              ★
            </span>
          ))}
        </div>

        {/* Comment box */}
        <textarea
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Write your feedback..."
          className="w-full border rounded-md p-2 text-sm mb-4 resize-none h-24"
        />

        {/* Buttons */}
        <div className="flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-md bg-gray-300 hover:bg-gray-400 text-sm"
            disabled={loading}>
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            className="px-4 py-2 rounded-md bg-blue-600 hover:bg-blue-700 text-white text-sm"
            disabled={loading}>
            {loading ? "Submitting..." : "Submit"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReviewModal;
