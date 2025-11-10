/**
 * @jest-environment jsdom
 */

import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import FindTutor from "@/pages/student/FindTutor";
import { Provider } from "react-redux";
import store from "@/redux/store";
import { MemoryRouter } from "react-router-dom";
import { SearchTutors } from "@/api/studentAPI";
import * as storeModule from "@/redux/store"; // import full store module

// 🧩 Mock API modules (avoid import.meta issues)
jest.mock("@/api/studentAPI", () => ({
  SearchTutors: jest.fn(),
}));
jest.mock("@/api/adminAPI", () => ({}));
jest.mock("@/api/tutorAPI", () => ({}));
jest.mock("@/api/notificationAPI", () => ({}));

// 🧩 Mock router navigation
const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

// 🧩 Mock Redux selector only (keep actual store intact)
jest
  .spyOn(storeModule, "useAppSelector")
  .mockImplementation((selector: any) => selector({ user: { user: { token: "mock-token" } } }));

// 🧩 Mock image import
jest.mock("@/assets/default-profile-pic.jpg", () => "test-file-stub");

// 🧩 Helper render function
const renderComponent = () =>
  render(
    <Provider store={store}>
      <MemoryRouter>
        <FindTutor />
      </MemoryRouter>
    </Provider>
  );

describe("FindTutor Page", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders header and search form", async () => {
    (SearchTutors as jest.Mock).mockResolvedValueOnce({ data: [] });

    renderComponent();

    expect(await screen.findByText(/Find your tutor now/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Search by tutor name/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Search/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Clear Filters/i })).toBeInTheDocument();
  });

  test("shows error if token is missing", async () => {
    jest
      .spyOn(storeModule, "useAppSelector")
      .mockImplementationOnce((selector: any) => selector({ user: { user: null } }));

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/No auth token found/i)).toBeInTheDocument();
    });
  });

  test("displays tutor cards when API returns data", async () => {
    const mockTutors = [
      {
        id: 1,
        firstName: "Alice",
        lastName: "Tan",
        subject: "Math",
        hourlyRate: 50,
        availability: {
          Monday: { enabled: true, start: "09:00", end: "12:00" },
        },
        profileImageUrl: "mock-img.jpg",
      },
      {
        id: 2,
        firstName: "Bob",
        lastName: "Lee",
        subject: "Science",
        hourlyRate: 60,
        availability: {
          Tuesday: { enabled: true, start: "14:00", end: "17:00" },
        },
        profileImageUrl: null,
      },
    ];

    (SearchTutors as jest.Mock).mockResolvedValueOnce({ data: mockTutors });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/Alice Tan/)).toBeInTheDocument();
      expect(screen.getByText(/Bob Lee/)).toBeInTheDocument();
      expect(screen.getByText(/Teaches: Math/i)).toBeInTheDocument();
      expect(screen.getByText(/Teaches: Science/i)).toBeInTheDocument();
    });

    // "View Profile" navigation
    const buttons = screen.getAllByRole("button", { name: /View Profile/i });
    expect(buttons).toHaveLength(2);

    fireEvent.click(buttons[0]);
    expect(mockNavigate).toHaveBeenCalledWith("/student/view-tutor/1");
  });

  test("handles API error gracefully", async () => {
    (SearchTutors as jest.Mock).mockRejectedValueOnce(new Error("API fail"));

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/Failed to fetch tutors/i)).toBeInTheDocument();
    });
  });
});
