/**
 * @jest-environment jsdom
 */

import { render, screen, fireEvent } from "@testing-library/react";
import WalletCancelPage from "@/pages/student/WalletCancelPage";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import store from "@/redux/store";

// 🧩 Mock Navbar
jest.mock("@/components/Navbar", () => () => <div data-testid="navbar">Mock Navbar</div>);

// 🧩 Mock navigation
const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

const renderComponent = () =>
  render(
    <Provider store={store}>
      <MemoryRouter>
        <WalletCancelPage />
      </MemoryRouter>
    </Provider>
  );

describe("WalletCancelPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders header and description", () => {
    renderComponent();

    expect(screen.getByTestId("navbar")).toBeInTheDocument();
    expect(screen.getByText(/Payment Cancelled/i)).toBeInTheDocument();
    expect(screen.getByText(/Your payment was not completed/i)).toBeInTheDocument();
  });

  test("navigates to wallet when Back to Wallet clicked", () => {
    renderComponent();

    const backBtn = screen.getByRole("button", { name: /Back to Wallet/i });
    fireEvent.click(backBtn);

    expect(mockNavigate).toHaveBeenCalledWith("/student/wallet?status=cancelled");
  });

  test("navigates to dashboard when Go to Dashboard clicked", () => {
    renderComponent();

    const dashBtn = screen.getByRole("button", { name: /Go to Dashboard/i });
    fireEvent.click(dashBtn);

    expect(mockNavigate).toHaveBeenCalledWith("/student/dashboard");
  });
});
