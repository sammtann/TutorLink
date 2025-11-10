/**
 * @jest-environment jsdom
 */

import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import StudentDashboard from "@/pages/student/StudentDashboard";
import { Provider } from "react-redux";
import store from "@/redux/store";
import { MemoryRouter } from "react-router-dom";
import * as storeModule from "@/redux/store";
import {
  GetStudentByUserId,
} from "@/api/studentAPI";
import {
  GetBookingsForStudent,
  CancelBooking,
} from "@/api/bookingAPI";
import { GetWalletByUserId } from "@/api/walletAPI";

// -------------------------
// 🔧 Mocks
// -------------------------
jest.mock("@/api/studentAPI", () => ({
  GetStudentByUserId: jest.fn(),
}));
jest.mock("@/api/bookingAPI", () => ({
  GetBookingsForStudent: jest.fn(),
  CancelBooking: jest.fn(),
}));
jest.mock("@/api/walletAPI", () => ({
  GetWalletByUserId: jest.fn(),
}));
jest.mock("@/components/Navbar", () => () => <div data-testid="navbar">Mock Navbar</div>);
jest.mock("@/components/ProfilePicModal", () => (props: any) =>
  props.isOpen ? (
    <div data-testid="profile-modal">
      Mock Profile Modal
      <button onClick={props.onClose}>Close</button>
    </div>
  ) : null
);
jest.mock("@/components/BookingCard", () => (props: any) => (
  <div data-testid="booking-card">
    BookingCard - {props.id}
    <button onClick={() => props.onCancel(props.id)}>Cancel</button>
    <button onClick={() => props.onReschedule(props.id, "t1", "Tutor Tom", "Student Sam")}>
      Reschedule
    </button>
  </div>
));
jest.mock("@/components/RescheduleModal", () => (props: any) =>
  props.booking ? (
    <div data-testid="reschedule-modal">
      Reschedule Modal for {props.booking.bookingId}
      <button onClick={props.onClose}>Close</button>
    </div>
  ) : null
);

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

const mockToastSuccess = jest.fn();
const mockToastError = jest.fn();
jest.mock("react-toastify", () => ({
  toast: {
    success: (msg: string) => mockToastSuccess(msg),
    error: (msg: string) => mockToastError(msg),
  },
}));

// Silence console noise
beforeAll(() => jest.spyOn(console, "error").mockImplementation(() => {}));
afterAll(() => (console.error as jest.Mock).mockRestore());

// Mock Redux selector for user
jest.spyOn(storeModule, "useAppSelector").mockImplementation((selector: any) =>
  selector({
    user: {
      user: {
        id: "student1",
        token: "mock-token",
        name: "Alice",
        email: "alice@mail.com",
      },
    },
  })
);

// -------------------------
// Helper render
// -------------------------
const renderComponent = () =>
  render(
    <Provider store={store}>
      <MemoryRouter>
        <StudentDashboard />
      </MemoryRouter>
    </Provider>
  );

// -------------------------
// 🧪 TESTS
// -------------------------
describe("StudentDashboard", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (window.confirm as any) = jest.fn(() => true);
  });

  it("renders and fetches student details, bookings, and wallet", async () => {
    (GetStudentByUserId as jest.Mock).mockResolvedValueOnce({
      data: { studentNumber: "S123", gradeLevel: "Sec 3", profileImageUrl: "" },
    });
    (GetBookingsForStudent as jest.Mock).mockResolvedValueOnce({ data: [] });
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 80 },
    });

    renderComponent();

    expect(screen.getByTestId("navbar")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/Welcome to your Dashboard/i)).toBeInTheDocument();
      expect(screen.getByText(/SGD 80.00/i)).toBeInTheDocument();
      expect(screen.getByText(/Student Profile/i)).toBeInTheDocument();
    });
  });

  it("shows empty bookings message when no sessions", async () => {
    (GetStudentByUserId as jest.Mock).mockResolvedValueOnce({
      data: { studentNumber: "S123", gradeLevel: "P6" },
    });
    (GetBookingsForStudent as jest.Mock).mockResolvedValueOnce({ data: [] });
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({ data: { balance: 0 } });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/No upcoming sessions yet/i)).toBeInTheDocument();
    });
  });

  it("renders a booking card and allows cancellation", async () => {
    (GetStudentByUserId as jest.Mock).mockResolvedValueOnce({
      data: { studentNumber: "S456", gradeLevel: "JC1" },
    });
    (GetBookingsForStudent as jest.Mock).mockResolvedValueOnce({
      data: [{ id: "b1", date: "2099-10-31", start: "10:00", status: "confirmed" }],
    });
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({ data: { balance: 40 } });
    (CancelBooking as jest.Mock).mockResolvedValueOnce({});

    renderComponent();

    await waitFor(() => {
      expect(screen.getByTestId("booking-card")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Cancel/i));

    await waitFor(() => {
      expect(CancelBooking).toHaveBeenCalled();
      expect(mockToastSuccess).toHaveBeenCalledWith("Booking cancelled successfully");
    });
  });

  it("handles cancel booking API error", async () => {
    (GetStudentByUserId as jest.Mock).mockResolvedValueOnce({
      data: { studentNumber: "S789", gradeLevel: "Sec 4" },
    });
    (GetBookingsForStudent as jest.Mock).mockResolvedValueOnce({
      data: [{ id: "b2", date: "2099-10-31", start: "10:00", status: "confirmed" }],
    });
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({ data: { balance: 10 } });
    (CancelBooking as jest.Mock).mockRejectedValueOnce({
      response: { data: { message: "Failed to cancel booking" } },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByTestId("booking-card")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Cancel/i));

    await waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith("Failed to cancel booking");
    });
  });

  it("navigates to wallet and past sessions when clicked", async () => {
    (GetStudentByUserId as jest.Mock).mockResolvedValueOnce({
      data: { studentNumber: "S321", gradeLevel: "JC2" },
    });
    (GetBookingsForStudent as jest.Mock).mockResolvedValueOnce({ data: [] });
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 123 },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/Current Balance/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Current Balance/i));
    expect(mockNavigate).toHaveBeenCalledWith("/student/wallet");

    fireEvent.click(screen.getByText(/View Past Sessions/i));
    expect(mockNavigate).toHaveBeenCalledWith("/student/past-sessions");
  });

  it("opens reschedule modal when reschedule clicked", async () => {
    (GetStudentByUserId as jest.Mock).mockResolvedValueOnce({
      data: { studentNumber: "S100", gradeLevel: "Sec 2" },
    });
    (GetBookingsForStudent as jest.Mock).mockResolvedValueOnce({
      data: [{ id: "b5", date: "2099-10-31", start: "09:00", status: "confirmed" }],
    });
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 50 },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByTestId("booking-card")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Reschedule/i));

    await waitFor(() => {
      expect(screen.getByTestId("reschedule-modal")).toBeInTheDocument();
    });
  });
});
