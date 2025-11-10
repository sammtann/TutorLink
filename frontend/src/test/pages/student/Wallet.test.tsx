/**
 * @jest-environment jsdom
 */

import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";
import Wallet from "@/pages/student/Wallet";
import { Provider } from "react-redux";
import store from "@/redux/store";
import { MemoryRouter } from "react-router-dom";
import * as storeModule from "@/redux/store";
import {
  GetWalletByUserId,
  GetWalletTransactions,
  CreateCheckoutSession,
  SetWalletPin,
  WithdrawAllFunds,
} from "@/api/walletAPI";

// 🧩 Mock API calls
jest.mock("@/api/walletAPI", () => ({
  GetWalletByUserId: jest.fn(),
  GetWalletTransactions: jest.fn(),
  CreateCheckoutSession: jest.fn(),
  SetWalletPin: jest.fn(),
  WithdrawAllFunds: jest.fn(),
}));

// 🧩 Mock Navbar
jest.mock("@/components/Navbar", () => () => <div data-testid="navbar">Mock Navbar</div>);

// 🧩 Mock toast
const mockToastSuccess = jest.fn();
const mockToastError = jest.fn();
const mockToastInfo = jest.fn();
jest.mock("react-toastify", () => ({
  toast: {
    success: (msg: string) => mockToastSuccess(msg),
    error: (msg: string) => mockToastError(msg),
    info: (msg: string) => mockToastInfo(msg),
  },
}));

// 🧩 Mock navigation + searchParams
const mockNavigate = jest.fn();
const mockSetSearchParams = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
  useSearchParams: () => [new URLSearchParams("status=success"), mockSetSearchParams],
}));

// 🧩 Mock Redux user
jest.spyOn(storeModule, "useAppSelector").mockImplementation((selector: any) =>
  selector({
    user: { user: { id: "u1", token: "mock-token", name: "Alice" } },
  })
);

// 🧩 Render helper
const renderComponent = () =>
  render(
    <Provider store={store}>
      <MemoryRouter>
        <Wallet />
      </MemoryRouter>
    </Provider>
  );

describe("WalletPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders loading state and fetches wallet successfully", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 100, pinSet: true },
    });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({
      data: [
        { id: "t1", type: "Top-up", amount: 50, description: "Added funds", date: new Date() },
      ],
    });

    renderComponent();

    expect(screen.getByText(/Loading wallet/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/100.00 SGD/i)).toBeInTheDocument();
      expect(screen.getByText(/Transaction History/i)).toBeInTheDocument();
      expect(mockToastSuccess).toHaveBeenCalledWith("Credits added successfully!");
    });
  });

  test("shows empty transaction history message", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({ data: { balance: 0, pinSet: false } });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/No transactions yet/i)).toBeInTheDocument();
    });
  });

  test("handles fetch failure gracefully", async () => {
    (GetWalletByUserId as jest.Mock).mockRejectedValueOnce(new Error("fail"));
    (GetWalletTransactions as jest.Mock).mockRejectedValueOnce(new Error("fail"));

    renderComponent();

    await waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith("Failed to fetch wallet details");
    });
  });

  test("opens and cancels top-up confirmation modal", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({ data: { balance: 10, pinSet: true } });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/Current Balance/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Top-up 10/i));
    expect(screen.getByText(/Confirm Top-up/i)).toBeInTheDocument();

    fireEvent.click(screen.getByText(/Cancel/i));
    await waitFor(() => {
      expect(screen.queryByText(/Confirm Top-up/i)).not.toBeInTheDocument();
    });
  });

  test("shows error when Stripe session fails", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({ data: { balance: 20, pinSet: true } });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });
    (CreateCheckoutSession as jest.Mock).mockResolvedValueOnce({ data: {} });

    renderComponent();

    await waitFor(() => screen.getByText(/Top-up 10/i));

    fireEvent.click(screen.getByText(/Top-up 10/i));
    fireEvent.click(screen.getByText(/Yes, Top-up/i));

    await waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith("Failed to create Stripe session");
    });
  });

  test("opens wallet PIN modal for withdraw", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 100, pinSet: true },
    });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });

    renderComponent();

    await waitFor(() => screen.getByText(/Withdraw All Funds/i));

    fireEvent.click(screen.getByText(/Withdraw All Funds/i));
    expect(screen.getByText(/Enter Wallet PIN/i)).toBeInTheDocument();
  });

  test("sets new wallet PIN successfully", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 50, pinSet: false },
    });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });
    (SetWalletPin as jest.Mock).mockResolvedValueOnce({});

    renderComponent();

    await waitFor(() => screen.getByText(/Withdraw All Funds/i));

    fireEvent.click(screen.getByText(/Withdraw All Funds/i));
    const input = screen.getByPlaceholderText(/Enter 4-6 digit PIN/i);
    fireEvent.change(input, { target: { value: "1234" } });
    fireEvent.click(screen.getByText(/Set PIN/i));

    await waitFor(() => {
      expect(mockToastSuccess).toHaveBeenCalledWith("Wallet PIN set successfully");
    });
  });

  test("handles withdraw success flow", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 100, pinSet: true },
    });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });
    (WithdrawAllFunds as jest.Mock).mockResolvedValueOnce({
      data: { message: "Withdrawal successful" },
    });

    renderComponent();

    await waitFor(() => screen.getByText(/Withdraw All Funds/i));

    fireEvent.click(screen.getByText(/Withdraw All Funds/i));
    const input = screen.getByPlaceholderText(/Enter 4-6 digit PIN/i);
    fireEvent.change(input, { target: { value: "4321" } });
    fireEvent.click(screen.getByText(/Confirm Withdraw/i));

    await waitFor(() => {
      expect(mockToastSuccess).toHaveBeenCalledWith("Withdrawal successful");
      expect(WithdrawAllFunds).toHaveBeenCalledWith("u1", "4321", "mock-token");
    });
  });

  test("shows PIN validation error for too short PIN", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 100, pinSet: false },
    });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });

    renderComponent();

    await waitFor(() => screen.getByText(/Withdraw All Funds/i));
    fireEvent.click(screen.getByText(/Withdraw All Funds/i));

    const input = screen.getByPlaceholderText(/Enter 4-6 digit PIN/i);
    fireEvent.change(input, { target: { value: "12" } });
    fireEvent.click(screen.getByText(/Set PIN/i));

    expect(mockToastError).toHaveBeenCalledWith("PIN must be at least 4 digits");
  });

  test("handles withdraw API error", async () => {
    (GetWalletByUserId as jest.Mock).mockResolvedValueOnce({
      data: { balance: 100, pinSet: true },
    });
    (GetWalletTransactions as jest.Mock).mockResolvedValueOnce({ data: [] });
    (WithdrawAllFunds as jest.Mock).mockRejectedValueOnce({
      response: { data: { error: "Invalid PIN" } },
    });

    renderComponent();

    await waitFor(() => screen.getByText(/Withdraw All Funds/i));

    fireEvent.click(screen.getByText(/Withdraw All Funds/i));
    const input = screen.getByPlaceholderText(/Enter 4-6 digit PIN/i);
    fireEvent.change(input, { target: { value: "9999" } });
    fireEvent.click(screen.getByText(/Confirm Withdraw/i));

    await waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith("Invalid PIN");
    });
  });
});
