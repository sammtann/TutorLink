/**
 * @jest-environment jsdom
 */

import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import PastSessionsPage from "@/pages/student/PastSessionsPage";
import { Provider } from "react-redux";
import store from "@/redux/store";
import { MemoryRouter } from "react-router-dom";
import { GetPastSessionsForStudent } from "@/api/bookingAPI";
import * as storeModule from "@/redux/store";

// 🧩 Mock dependencies
jest.mock("@/api/bookingAPI", () => ({
  GetPastSessionsForStudent: jest.fn(),
}));

jest.mock("@/components/Navbar", () => () => <div data-testid="navbar">Mock Navbar</div>);
jest.mock("@/components/BookingCard", () => (props: any) => (
  <div data-testid="booking-card">
    <span>{props.subject || "Sample Subject"}</span>
    <button onClick={props.onReview}>Leave Review</button>
  </div>
));
jest.mock(
  "@/components/ReviewModal",
  () => (props: any) =>
    props.isOpen ? (
      <div data-testid="review-modal">
        Review Modal Open for tutor {props.tutorId}, booking {props.bookingId}
        <button onClick={props.onClose}>Close</button>
      </div>
    ) : null
);

// 🧩 Mock toast and navigation
const mockNavigate = jest.fn();
const mockToastError = jest.fn();
jest.mock("react-toastify", () => ({
  toast: { error: (msg: string) => mockToastError(msg) },
}));
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

// 🧩 Mock Redux selector (simulate logged in user)
jest.spyOn(storeModule, "useAppSelector").mockImplementation((selector: any) =>
  selector({
    user: { user: { id: "u1", token: "mock-token", name: "Alice" } },
  })
);

const renderComponent = () =>
  render(
    <Provider store={store}>
      <MemoryRouter>
        <PastSessionsPage />
      </MemoryRouter>
    </Provider>
  );

describe("PastSessionsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders loading state initially", async () => {
    (GetPastSessionsForStudent as jest.Mock).mockResolvedValueOnce({
      data: { recentSessions: [] },
    });

    renderComponent();

    // while useEffect runs
    expect(screen.getByText(/Loading past sessions/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/No past sessions found/i)).toBeInTheDocument();
    });
  });

  test("redirects and shows toast if user not logged in", async () => {
    jest
      .spyOn(storeModule, "useAppSelector")
      .mockImplementationOnce((selector: any) => selector({ user: { user: null } }));

    renderComponent();

    await waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith("User not logged in");
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  test("shows empty state when no sessions returned", async () => {
    (GetPastSessionsForStudent as jest.Mock).mockResolvedValueOnce({
      data: { recentSessions: [] },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/No past sessions found/i)).toBeInTheDocument();
    });
  });

  test("renders list of past sessions when API returns data", async () => {
    const mockSessions = [
      { id: "b1", tutorId: "t1", subject: "Math", status: "CONFIRMED" },
      { id: "b2", tutorId: "t2", subject: "Science", status: "CONFIRMED" },
    ];
    (GetPastSessionsForStudent as jest.Mock).mockResolvedValueOnce({
      data: { recentSessions: mockSessions },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getAllByTestId("booking-card")).toHaveLength(2);
      expect(screen.getByText("Math")).toBeInTheDocument();
      expect(screen.getByText("Science")).toBeInTheDocument();
    });
  });

  test("handles API failure gracefully", async () => {
    (GetPastSessionsForStudent as jest.Mock).mockRejectedValueOnce(new Error("Network fail"));

    renderComponent();

    await waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith("Failed to fetch past sessions");
    });
  });

  test("opens and closes ReviewModal when clicking review", async () => {
    const mockSessions = [{ id: "b1", tutorId: "t1", subject: "Math", status: "CONFIRMED" }];
    (GetPastSessionsForStudent as jest.Mock).mockResolvedValueOnce({
      data: { recentSessions: mockSessions },
    });

    renderComponent();

    // wait for sessions
    await waitFor(() => {
      expect(screen.getByText("Math")).toBeInTheDocument();
    });

    // open review modal
    fireEvent.click(screen.getByText(/Leave Review/i));
    expect(screen.getByTestId("review-modal")).toBeInTheDocument();

    // close modal
    fireEvent.click(screen.getByText(/Close/i));
    await waitFor(() => {
      expect(screen.queryByTestId("review-modal")).not.toBeInTheDocument();
    });
  });
});
