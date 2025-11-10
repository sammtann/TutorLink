import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import ViewTutorDetails from "@/pages/student/ViewTutorDetails";
import { Provider } from "react-redux";
import store from "@/redux/store";
import { MemoryRouter } from "react-router-dom";
import * as storeModule from "@/redux/store";
import { GetTutorById } from "@/api/studentAPI";
import { CreateBooking, GetBookingsForTutorRange } from "@/api/bookingAPI";

// -------------------------
// 🔧 MOCK SETUP
// -------------------------
jest.mock("@/api/studentAPI", () => ({
  GetTutorById: jest.fn(),
}));
jest.mock("@/api/bookingAPI", () => ({
  CreateBooking: jest.fn(),
  GetBookingsForTutorRange: jest.fn(),
}));
jest.mock("@/components/Navbar", () => () => <div data-testid="navbar">Mock Navbar</div>);
jest.mock("@/components/AvailabilityCalendar", () => (props: any) => (
  <div
    data-testid="calendar"
    onClick={() =>
      props.onSlotClick(new Date("2025-01-01T10:00:00"), { start: "10:00", end: "11:00" })
    }>
    Mock Calendar
  </div>
));
jest.mock("@/components/BookingModal", () => (props: any) => (
  <div data-testid="modal">
    <button onClick={() => props.onConfirm("Beginner Lesson")}>Confirm</button>
    <button onClick={props.onClose}>Close</button>
  </div>
));

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () => ({ id: "t1" }),
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

// Mock Redux selector for user
jest
  .spyOn(storeModule, "useAppSelector")
  .mockImplementation((selector: any) =>
    selector({ user: { user: { id: "u1", token: "mock-token", name: "Alice" } } })
  );

// Silence noisy console errors
beforeAll(() => {
  jest.spyOn(console, "error").mockImplementation(() => {});
});
afterAll(() => {
  (console.error as jest.Mock).mockRestore();
});

// -------------------------
// 🔧 Helper Render Function
// -------------------------
const renderComponent = () =>
  render(
    <Provider store={store}>
      <MemoryRouter>
        <ViewTutorDetails />
      </MemoryRouter>
    </Provider>
  );

// -------------------------
// 🧪 TESTS
// -------------------------
describe("ViewTutorDetails", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders loading state and fetches tutor", async () => {
    (GetTutorById as jest.Mock).mockResolvedValueOnce({
      data: {
        userId: "t1",
        firstName: "John",
        lastName: "Doe",
        hourlyRate: 50,
        subject: "Math, Science",
        description: "Experienced tutor",
        lessonType: ["Beginner Lesson"],
        qualifications: [],
        reviews: [],
        availability: {},
      },
    });
    (GetBookingsForTutorRange as jest.Mock).mockResolvedValueOnce({ data: [] });

    renderComponent();

    // Should show loading first
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();

    // Tutor name should appear
    await waitFor(() => {
      expect(screen.getByText(/John Doe/i)).toBeInTheDocument();
    });
  });

  it("renders qualifications and reviews correctly", async () => {
    (GetTutorById as jest.Mock).mockResolvedValueOnce({
      data: {
        userId: "t1",
        firstName: "John",
        lastName: "Doe",
        hourlyRate: 60,
        subject: "Physics",
        description: "Expert tutor",
        lessonType: ["Advanced Lesson"],
        qualifications: [
          { name: "PhD", type: "Degree", uploadedAt: new Date().toISOString(), path: "#" },
        ],
        reviews: [{ studentName: "Jane", rating: 5, comment: "Excellent!" }],
        availability: {},
      },
    });
    (GetBookingsForTutorRange as jest.Mock).mockResolvedValueOnce({ data: [] });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/PhD/i)).toBeInTheDocument();
      expect(screen.getByText(/Excellent/i)).toBeInTheDocument();
    });
  });
});
