import { useNavigate } from "react-router-dom";
import { useAppSelector } from "@/redux/store";
import { GetStudentByUserId } from "@/api/studentAPI";
import { GetBookingsForStudent, CancelBooking } from "@/api/bookingAPI";
import { GetWalletByUserId } from "@/api/walletAPI";
import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import Navbar from "@/components/Navbar";
import ProfilePicModal from "@/components/ProfilePicModal";
import BookingCard from "@/components/BookingCard";
import defaultProfile from "../../assets/default-profile-pic.jpg";
import { BookingResponse } from "@/types/BookingType";
import RescheduleModal from "@/components/RescheduleModal";

const StudentDashboard = () => {
  const { user } = useAppSelector((state) => state.user);
  const navigate = useNavigate();

  const [studentDetails, setStudentDetails] = useState<StudentDetails | null>(null);
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [walletBalance, setWalletBalance] = useState<number>(0);
  const [loadingWallet, setLoadingWallet] = useState<boolean>(true);
  const [showOnlyConfirmed, setShowOnlyConfirmed] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [rescheduleBooking, setRescheduleBooking] = useState<{
    bookingId: string;
    tutorId: string;
    tutorName: string;
    studentName: string;
  } | null>(null);

  const fetchWallet = async (studentId: string) => {
    if (!user?.token) return;
    try {
      const res = await GetWalletByUserId(studentId, user.token);
      setWalletBalance(res.data.balance ?? 0);
    } catch {
      toast.error("Failed to fetch wallet balance");
    } finally {
      setLoadingWallet(false);
    }
  };

  const fetchStudentDetails = async (id: string) => {
    if (!user?.token) return;
    try {
      const res = await GetStudentByUserId(id, user.token);
      setStudentDetails(res.data);
    } catch {
      toast.error("Failed to fetch student details");
    }
  };

  const fetchBookings = async (studentId: string) => {
    if (!user?.token) return;
    try {
      const res = await GetBookingsForStudent(studentId, user.token);
      setBookings(res.data);
    } catch {
      toast.error("Failed to fetch bookings");
    }
  };

  const handleReschedule = (bookingId: string, tutorId: string, tutorName: string, studentName: string) => {
    setRescheduleBooking({ bookingId, tutorId, tutorName, studentName});
  };

  const handleCancelBooking = async (bookingId: string) => {
    if (!user?.token) return;
    const confirmCancel = window.confirm("Are you sure you want to cancel this session?");
    if (!confirmCancel) return;

    try {
      await CancelBooking(bookingId, user.id, user.token);
      toast.success("Booking cancelled successfully");
      await Promise.all([fetchBookings(user.id), fetchWallet(user.id)]);
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Failed to cancel booking");
    }
  };

  useEffect(() => {
    if (!user?.id || !user?.token) {
      toast.error("User not logged in");
      navigate("/");
      return;
    }
    fetchStudentDetails(user.id);
    fetchBookings(user.id);
    fetchWallet(user.id);
  }, [user, navigate]);

  const now = new Date();
  let upcomingBookings = bookings.filter((b) => new Date(`${b.date}T${b.start}`) >= now);
  if (showOnlyConfirmed) {
    upcomingBookings = upcomingBookings.filter((b) => b.status === "confirmed");
  }

  return (
    <div className="h-screen flex flex-col">
      {/* Navbar */}
      <Navbar />

      {/* Main Content */}
      <div className="flex-1 bg-[#f2f2f2] p-6 overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex justify-between items-center mb-5">
          <div className="flex items-center gap-4">
            <h1 className="font-bold text-xl">Welcome to your Dashboard!</h1>
          </div>
        </div>

        {/* Main Grid */}
        <div className="flex flex-1 gap-6 overflow-hidden">
          {/* Left: Upcoming */}
          <div className="flex flex-col w-[70%] bg-white rounded-md shadow-md p-5 overflow-hidden">
            <div className="flex justify-between items-center mb-3">
              <h2 className="font-bold text-lg">Upcoming Sessions</h2>
              <button
                onClick={() => setShowOnlyConfirmed((prev) => !prev)}
                className="px-3 py-1 border rounded-md text-sm bg-primary text-white hover:bg-gray-200 hover:text-black transition">
                {showOnlyConfirmed ? "Show All" : "Hide Pending/Cancelled"}
              </button>
            </div>

            <div className="flex-1 overflow-y-auto pr-2">
              {upcomingBookings.length === 0 ? (
                <div className="h-40 flex items-center justify-center text-gray-400">
                  No upcoming sessions yet.
                </div>
              ) : (
                <div className="grid grid-cols-1 gap-y-4">
                  {upcomingBookings.map((b) => (
                    <BookingCard
                      key={b.id}
                      {...b}
                      onCancel={handleCancelBooking}
                      onReschedule={handleReschedule}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Right: Profile + Wallet + Past Session */}
          <div className="flex flex-col w-[30%] h-full">
            <div className="flex flex-col flex-1 overflow-y-auto gap-4 p-1">
              {/* Profile Card */}
              <div className="bg-white rounded-md shadow-md p-5 text-center flex-shrink-0">
                <h1 className="font-bold text-xl">Student Profile</h1>
                {studentDetails ? (
                  <div className="mt-4 text-left">
                    <div className="flex justify-center mb-3">
                      <img
                        src={studentDetails.profileImageUrl || defaultProfile}
                        alt="Profile"
                        className="w-24 h-24 rounded-full object-cover border"
                      />
                    </div>
                    <p>
                      <strong>Full Name:</strong> {user?.name || "N/A"}
                    </p>
                    <p>
                      <strong>Email:</strong> {user?.email || "N/A"}
                    </p>
                    <p>
                      <strong>Student No:</strong> {studentDetails.studentNumber}
                    </p>
                    <p>
                      <strong>Grade Level:</strong> {studentDetails.gradeLevel}
                    </p>

                    <div className="mt-4 flex justify-center gap-2">
                      <button
                        onClick={() => setIsModalOpen(true)}
                        className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition">
                        Change Profile Pic
                      </button>
                    </div>

                    <ProfilePicModal
                      isOpen={isModalOpen}
                      onClose={() => setIsModalOpen(false)}
                      refreshProfile={() => {
                        if (user?.id) fetchStudentDetails(user.id);
                      }}
                      userType="student"
                    />
                  </div>
                ) : (
                  <p>Loading student details...</p>
                )}
              </div>

              {/* Wallet Card */}
              <div
                onClick={() => navigate("/student/wallet")}
                className="bg-white rounded-md shadow-md p-5 cursor-pointer hover:shadow-lg transition flex-shrink-0">
                <div className="flex justify-between items-center">
                  <div className="text-left">
                    <h2 className="font-bold text-lg text-gray-800">Current Balance</h2>

                    {!loadingWallet ? (
                      <p className="text-2xl font-bold text-green-600 mt-1">
                        SGD {walletBalance.toFixed(2)}
                      </p>
                    ) : (
                      <div className="text-gray-400 text-sm">Loading wallet...</div>
                    )}
                  </div>

                  <button
                    className="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/90 transition"
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate("/student/wallet");
                    }}>
                    Manage Wallet
                  </button>
                </div>
              </div>

              {/* Past Sessions Giant Card */}
              <div
                onClick={() => navigate("/student/past-sessions")}
                className="bg-gradient-to-r from-blue-500 to-indigo-600 text-white text-center p-6 rounded-md shadow-md cursor-pointer hover:opacity-90 transition flex-1 flex items-center justify-center">
                <div>
                  <h2 className="text-xl font-semibold">View Past Sessions</h2>
                  <p className="text-sm mt-1 opacity-90">
                    Tap here to review your completed lessons
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      {/* Reschedule Modal */}
      {rescheduleBooking && (
        <RescheduleModal
          booking={rescheduleBooking}
          onClose={() => setRescheduleBooking(null)}
          onRescheduleConfirmed={() => {
            setRescheduleBooking(null);
            if (user?.id) fetchBookings(user.id);
          }}
        />
      )}

      {/* Footer */}
      <footer className="bg-white text-gray-600 text-sm text-center py-3 border-t shadow-inner">
        © {new Date().getFullYear()} TutorLink — Empowering Students & Tutors 🌱
      </footer>
    </div>
  );
};

export default StudentDashboard;
