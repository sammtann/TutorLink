import { useEffect, useState } from "react";
import { GetTutorProfile } from "@/api/tutorAPI";
import { GetBookingsForTutorRange, RequestReschedule } from "@/api/bookingAPI";
import AvailabilityCalendar, { TimeSlot } from "@/components/AvailabilityCalendar";
import BookingModal from "@/components/BookingModal";
import { useAppSelector } from "@/redux/store";
import { toast } from "react-toastify";

interface Props {
  booking: {
    bookingId: string;
    tutorId: string;
    tutorName: string;
    studentName: string;
  };
  onClose: () => void;
  onRescheduleConfirmed: () => void;
}

const RescheduleModal = ({ booking, onClose, onRescheduleConfirmed }: Props) => {
  const { user } = useAppSelector((state) => state.user);

  const [tutorAvailability, setTutorAvailability] = useState<Record<string, TimeSlot>>({});
  const [bookedSlots, setBookedSlots] = useState<{ date: string; status: string }[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<{ date: Date; slot: TimeSlot } | null>(null);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [lessonTypes, setLessonTypes] = useState<string[]>([]);

  // -------------------------------
  // Helper: format local date YYYY-MM-DD
  // -------------------------------
  const formatLocalDate = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  // -------------------------------
  // Fetch Tutor Availability
  // -------------------------------
  useEffect(() => {
    if (!user?.token || !booking.tutorId) return;
    const token = user.token;

    const fetchTutor = async () => {
      try {
        setLoading(true);
        const res = await GetTutorProfile(token, booking.tutorId);
        setTutorAvailability(res.data.availability || {});
        setLessonTypes(res.data.lessonType || ["Beginner", "Advanced"]);
      } catch (err) {
        console.error("Failed to fetch tutor availability", err);
        toast.error("Failed to load tutor availability");
      } finally {
        setLoading(false);
      }
    };

    fetchTutor();
  }, [booking.tutorId, user]);

  // -------------------------------
  // Fetch Tutor Booked Slots
  // -------------------------------
  useEffect(() => {
    if (!user?.token || !booking.tutorId) return;
    const token = user.token;

    const fetchBookings = async () => {
      try {
        const startDate = formatLocalDate(new Date());
        const endDate = formatLocalDate(new Date(new Date().setMonth(new Date().getMonth() + 3)));

        const res = await GetBookingsForTutorRange(booking.tutorId, startDate, endDate, token);
        setBookedSlots(res.data.map((b: any) => ({ date: b.date, status: b.status })));
      } catch (err) {
        console.error("Failed to fetch booked slots", err);
        toast.error("Failed to load booked slots");
      }
    };

    fetchBookings();
  }, [booking.tutorId, user]);

  // -------------------------------
  // Slot Click Handler
  // -------------------------------
  const handleSlotClick = (date: Date, slot: TimeSlot) => {
    setSelectedSlot({ date, slot });
    setShowBookingModal(true);
  };

  // -------------------------------
  // Confirm Reschedule
  // -------------------------------
  const confirmReschedule = async (lessonType: string) => {
    if (!selectedSlot || !user?.token) return;

    try {
      await RequestReschedule(
        booking.bookingId,
        {
          tutorId: booking.tutorId,
          studentId: user.id,
          tutorName: booking.tutorName,
          studentName: booking.studentName,
          date: formatLocalDate(selectedSlot.date), // ✅ use local date
          start: selectedSlot.slot.start,
          end: selectedSlot.slot.end,
          lessonType,
        },
        user.token
      );

      toast.success("Reschedule request submitted!");
      onRescheduleConfirmed();
    } catch (err) {
      console.error(err);
      toast.error("Failed to request reschedule");
    } finally {
      setShowBookingModal(false);
      setSelectedSlot(null);
    }
  };

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div className="bg-white rounded-md shadow-lg p-6 w-[90%] max-w-3xl h-[80%] flex items-center justify-center">
          <p className="text-gray-600">Loading availability...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-md shadow-lg p-6 w-[90%] max-w-3xl h-[80%] overflow-y-auto">
        <h2 className="font-bold text-xl mb-4">Select New Slot</h2>

        <AvailabilityCalendar
          role="student"
          availability={tutorAvailability}
          bookedSlots={bookedSlots}
          initialMonth={new Date()}
          onSlotClick={handleSlotClick}
          onMonthChange={() => {}}
        />

        {showBookingModal && selectedSlot && (
          <BookingModal
            lessonTypes={lessonTypes}
            slot={selectedSlot}
            onClose={() => setShowBookingModal(false)}
            onConfirm={confirmReschedule}
          />
        )}

        <button
          onClick={onClose}
          className="mt-4 px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 transition">
          Close
        </button>
      </div>
    </div>
  );
};

export default RescheduleModal;
