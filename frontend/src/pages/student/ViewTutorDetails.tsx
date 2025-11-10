import Navbar from "@/components/Navbar";
import { useParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { GetTutorById } from "@/api/studentAPI";
import { useAppSelector } from "@/redux/store";
import AvailabilityCalendar, {
  TimeSlot,
} from "@/components/AvailabilityCalendar";
import defaultProfile from "../../assets/default-profile-pic.jpg";
import { CreateBooking, GetBookingsForTutorRange } from "@/api/bookingAPI";
import BookingModal from "@/components/BookingModal";
import { toast } from "react-toastify";

const ViewTutorDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.user);

  const [tutor, setTutor] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedSlot, setSelectedSlot] = useState<{
    date: Date;
    slot: TimeSlot;
  } | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [bookedSlots, setBookedSlots] = useState<
    { date: string; status: string }[]
  >([]);
  const [monthStart, setMonthStart] = useState<Date>(() => {
    const today = new Date();
    return new Date(today.getFullYear(), today.getMonth(), 1);
  });

  // -------------------------
  // Fetch Tutor Details
  // -------------------------
  useEffect(() => {
    const fetchTutor = async () => {
      if (!id || !user?.token) return;

      try {
        setLoading(true);
        const res = await GetTutorById(id, user.token);
        const data = res.data;

        const tutorWithDefaults = {
          ...data,
          description: data.description || "No description provided",
          lessonType: data.lessonType ?? ["Beginner Lesson"],
          reviews: data.reviews ?? [],
        };

        setTutor(tutorWithDefaults);
        console.log("tutor details", res.data);
      } catch (err) {
        console.error("Failed to fetch tutor:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchTutor();
  }, [id, user]);

  // -------------------------
  // Fetch Bookings ONLY after tutor details are available
  // -------------------------
  useEffect(() => {
    if (!tutor || !user?.token) return;

    const tutorId = tutor.userId;
    const token = user.token;

    const fetchBookingsForMonth = async () => {
      const year = monthStart.getFullYear();
      const month = monthStart.getMonth();

      // first and last day as YYYY-MM-DD strings
      const firstDay = `${year}-${String(month + 1).padStart(2, "0")}-01`;
      const lastDay = `${year}-${String(month + 1).padStart(2, "0")}-${String(
        new Date(year, month + 1, 0).getDate()
      ).padStart(2, "0")}`;

      try {
        const res = await GetBookingsForTutorRange(
          tutorId,
          firstDay,
          lastDay,
          token
        );
        setBookedSlots(
          res.data.map((b: any) => ({ date: b.date, status: b.status }))
        );
        console.log("dates", res.data);
      } catch (err) {
        console.error("Failed to fetch bookings:", err);
      }
    };

    fetchBookingsForMonth();
  }, [tutor, monthStart, user]);

  const handleSlotClick = (date: Date, slot: TimeSlot) => {
    setSelectedSlot({ date, slot });
    setShowModal(true);
  };

  const confirmBooking = async (lessonType: string) => {
    if (!user?.token || !user?.id || !id || !selectedSlot) {
      alert("You must be logged in to book a lesson.");
      return;
    }

    const dateStr = selectedSlot.date.toLocaleDateString("en-CA");
    const [sh, sm] = selectedSlot.slot.start.split(":").map(Number);
    const [eh, em] = selectedSlot.slot.end.split(":").map(Number);
    let hours = (eh + em / 60) - (sh + sm / 60);

    // Handle overnight case (e.g. 09:00 → 00:00 or 23:00 → 01:00)
    if (hours < 0) {
      hours += 24;
    }

    const totalCost = tutor.hourlyRate * hours;

    if (totalCost <= 0 || isNaN(totalCost)) {
      toast.error("Invalid duration or hourly rate");
      return;
    }

    // ✅ Include `amount` in request
    const bookingReq = {
      tutorId: tutor.userId,
      studentId: user.id,
      tutorName: `${tutor.firstName} ${tutor.lastName}`,
      studentName: user.name,
      date: dateStr,
      start: selectedSlot.slot.start,
      end: selectedSlot.slot.end,
      lessonType,
      amount: totalCost,
    };

    try {
      await CreateBooking(bookingReq, user.token);
      setBookedSlots((prev) => [...prev, { date: dateStr, status: "pending" }]);
      toast.success(
        `Booking created. SGD ${totalCost.toFixed(2)} held temporarily.`
      );
    } catch (err: any) {
      console.error("Booking failed:", err);

      // Try to extract backend message
      const backendMsg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        err?.message ||
        "Failed to create booking";

      toast.error(backendMsg);
    } finally {
      setShowModal(false);
      setSelectedSlot(null);
    }
  };

  if (loading) return <p className="text-center mt-8">Loading...</p>;
  if (!tutor) return <p className="text-center mt-8">Tutor not found</p>;

  return (
    <div>
      <Navbar />
      <div className="min-h-screen bg-[#f9f9f9] p-6">
        <button
          onClick={() => navigate(-1)}
          className="mb-4 px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition"
        >
          ← Back
        </button>

        {/* Tutor Profile + Qualifications */}
        <div className="grid grid-cols-1 md:grid-cols-5 gap-6 mb-6">
          {/* Tutor Profile (60%) */}
          <div className="bg-white rounded-lg shadow-md p-6 flex flex-col md:flex-row gap-6 md:col-span-3 max-h-[320px]">
            <img
              src={tutor.profileImageUrl || defaultProfile}
              alt={tutor.firstName}
              className="w-32 h-32 rounded-full object-cover border shadow"
            />
            <div className="flex-1">
              <h1 className="text-3xl font-bold">
                {tutor.firstName} {tutor.lastName}
              </h1>
              {/* Truncate description with ellipsis */}
              <p className="text-gray-600 mt-3 line-clamp-6">
                {tutor.description}
              </p>

              {/* Subjects with Badge Style */}
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <span className="font-semibold text-gray-700">Subject:</span>
                {tutor.subject?.split(",").map((sub: string, idx: number) => (
                  <span
                    key={idx}
                    className="bg-blue-100 text-blue-800 text-sm font-semibold px-2 py-1 rounded-full"
                  >
                    {sub.trim()}
                  </span>
                ))}
              </div>

              <p className="mt-3 mb-4 text-primary font-bold text-xl">
                SGD {tutor.hourlyRate}/hr
              </p>
            </div>
          </div>

          {/* Qualifications (40%) */}
          <div className="bg-white rounded-lg shadow-md p-6 md:col-span-2 max-h-[320px] overflow-y-auto">
            <h2 className="text-xl font-semibold mb-3">Qualifications</h2>
            {tutor.qualifications && tutor.qualifications.length > 0 ? (
              <ul className="space-y-3">
                {tutor.qualifications.map((q: any, idx: number) => (
                  <li
                    key={idx}
                    className="border rounded-lg p-3 flex justify-between items-center"
                  >
                    <div>
                      <p className="font-semibold">{q.name}</p>
                      <p className="text-gray-500 text-sm">{q.type}</p>
                      {q.uploadedAt && (
                        <p className="text-xs text-gray-400">
                          Uploaded:{" "}
                          {new Date(q.uploadedAt).toLocaleDateString()}
                        </p>
                      )}
                    </div>
                    <a
                      href={q.path}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 hover:underline text-sm"
                    >
                      View
                    </a>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-gray-500">No qualifications uploaded.</p>
            )}
          </div>
        </div>

        {/* Lesson Types */}
        <div className="bg-white rounded-lg shadow-md mb-6 p-6">
          <h2 className="text-xl font-semibold mb-3">Lesson Types</h2>
          <ul className="list-disc pl-5 text-gray-700">
            {tutor.lessonType.map((type: string, idx: number) => (
              <li key={idx}>{type}</li>
            ))}
          </ul>
        </div>

        {/* Availability Calendar */}

        {/* Monthly Availability Calendar */}
        <AvailabilityCalendar
          role="student"
          availability={tutor.availability}
          bookedSlots={bookedSlots}
          initialMonth={monthStart}
          onSlotClick={handleSlotClick}
          onMonthChange={(newMonth) => setMonthStart(newMonth)}
        />
        {showModal && selectedSlot && (
          <BookingModal
            lessonTypes={
              tutor.lessonType || ["Beginner Lesson", "Advanced Lesson"]
            }
            slot={selectedSlot}
            hourlyRate={tutor.hourlyRate}
            onClose={() => setShowModal(false)}
            onConfirm={confirmBooking}
          />
        )}

        {/* Reviews */}
        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h2 className="text-xl font-semibold mb-3">Student Reviews</h2>
          {tutor.reviews.length > 0 ? (
            <div className="space-y-4">
              {tutor.reviews.map((review: any, idx: number) => (
                <div key={idx} className="border-b pb-3">
                  <p className="font-semibold">{review.studentName}</p>
                  <p className="text-yellow-500">{review.rating} ⭐</p>
                  <p className="text-gray-700">{review.comment}</p>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500">No reviews yet.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default ViewTutorDetails;
