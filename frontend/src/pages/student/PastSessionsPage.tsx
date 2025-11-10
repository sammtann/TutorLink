import { useEffect, useState } from "react";
import { useAppSelector } from "@/redux/store";
import { useNavigate } from "react-router-dom";
import Navbar from "@/components/Navbar";
import BookingCard from "@/components/BookingCard";
import ReviewModal from "@/components/ReviewModal";
import { GetPastSessionsForStudent } from "@/api/bookingAPI";
import { BookingResponse } from "@/types/BookingType";
import { toast } from "react-toastify";

const PastSessionsPage = () => {
  const { user } = useAppSelector((state) => state.user);
  const navigate = useNavigate();

  const [pastSessions, setPastSessions] = useState<BookingResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedTutorId, setSelectedTutorId] = useState<string | null>(null);
  const [selectedBookingId, setSelectedBookingId] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  // -------------------------
  // Fetch all past sessions
  // -------------------------
  useEffect(() => {
    if (!user?.id || !user?.token) {
      toast.error("User not logged in");
      navigate("/");
      return;
    }

    const fetchPastSessions = async () => {
      if (!user?.token) return;
      try {
        const res = await GetPastSessionsForStudent(user.id, user.token);
        setPastSessions(res.data.recentSessions || []);
      } catch (err) {
        console.error("Failed to fetch past sessions", err);
        toast.error("Failed to fetch past sessions");
      } finally {
        setLoading(false);
      }
    };

    fetchPastSessions();
  }, [user, navigate]);

  // -------------------------
  // Open review modal
  // -------------------------
  const handleOpenReview = (tutorId: string, bookingId: string) => {
    setSelectedTutorId(tutorId);
    setSelectedBookingId(bookingId);
    setIsModalOpen(true);
  };

  return (
    <div>
      <Navbar />
      <div className="min-h-screen bg-[#f2f2f2] p-6">
        <div className="flex justify-between items-center mb-5">
          <div className="flex items-center">
            <button
              onClick={() => navigate(-1)}
              className="mr-4 px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition"
            >
              ← Back
            </button>
            <h1 className="font-bold text-xl">My Past Sessions (Confirmed)</h1>
          </div>
        </div>

        {/* Loading / Empty / Content */}
        {loading ? (
          <div className="h-40 flex items-center justify-center text-gray-400">
            Loading past sessions...
          </div>
        ) : pastSessions.length === 0 ? (
          <div className="h-40 flex items-center justify-center text-gray-400">
            No past sessions found.
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {pastSessions.map((b) => (
              <div key={b.id} className="relative">
                <BookingCard
                  {...b}
                  isPastSession
                  onReview={() => handleOpenReview(b.tutorId!, b.id)} // ✅ Pass tutorId + bookingId
                />
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ✅ Review Modal */}
      {isModalOpen && selectedTutorId && selectedBookingId && user?.token && (
        <ReviewModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          tutorId={selectedTutorId}
          bookingId={selectedBookingId}
          studentName={user.name}
          token={user.token}
        />
      )}
    </div>
  );
};

export default PastSessionsPage;
