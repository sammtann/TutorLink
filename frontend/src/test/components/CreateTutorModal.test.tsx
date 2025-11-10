import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import CreateTutorModal from "@/components/CreateTutorModal";
import { RegisterUser } from "@/api/userAPI";
import { setLoading } from "@/redux/loaderSlice";
import { useAppDispatch } from "@/redux/store";
import { toast } from "react-toastify";

jest.mock("@/api/userAPI", () => ({
  RegisterUser: jest.fn(),
}));

jest.mock("@/redux/store", () => ({
  useAppDispatch: jest.fn(),
}));

jest.mock("react-toastify", () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

describe("CreateTutorModal", () => {
  const mockDispatch = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useAppDispatch as jest.Mock).mockReturnValue(mockDispatch);
  });

  const fillForm = () => {
    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "tutor@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("First Name"), {
      target: { value: "Alice" },
    });
    fireEvent.change(screen.getByPlaceholderText("Last Name"), {
      target: { value: "Tan" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "Pass1234" },
    });
  };

  // ---------- CLOSED ----------
  test("returns null when isOpen is false", () => {
    const { container } = render(<CreateTutorModal isOpen={false} onClose={mockOnClose} />);
    expect(container.firstChild).toBeNull();
  });

  // ---------- OPEN ----------
  test("renders all input fields when open", () => {
    render(<CreateTutorModal isOpen={true} onClose={mockOnClose} />);
    expect(screen.getByText("Add New Tutor")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Email")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("First Name")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Last Name")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
  });

  // ---------- VALIDATION ----------
  test("shows validation errors for empty required fields", async () => {
    render(<CreateTutorModal isOpen={true} onClose={mockOnClose} />);
    fireEvent.click(screen.getByText("Submit"));
    await waitFor(() => {
      expect(screen.getByText(/Valid email is required/)).toBeInTheDocument();
      expect(screen.getByText(/First name is required/)).toBeInTheDocument();
      expect(screen.getByText(/Last name is required/)).toBeInTheDocument();
      expect(screen.getByText(/Password is required/)).toBeInTheDocument();
    });
  });

  // ---------- SUCCESS SUBMIT ----------
  test("calls RegisterUser and shows success toast on success", async () => {
    (RegisterUser as jest.Mock).mockResolvedValue({
      status: 200,
      data: { message: "Tutor registered successfully" },
    });

    render(<CreateTutorModal isOpen={true} onClose={mockOnClose} />);
    fillForm();

    fireEvent.click(screen.getByText("Submit"));

    await waitFor(() => {
      expect(mockDispatch).toHaveBeenCalledWith(setLoading(true));
      expect(RegisterUser).toHaveBeenCalledWith(
        expect.objectContaining({
          firstname: "Alice",
          lastname: "Tan",
          email: "tutor@example.com",
          password: "Pass1234",
          role: "user",
        })
      );
      expect(toast.success).toHaveBeenCalledWith("Tutor registered successfully");
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  // ---------- FAILURE SUBMIT ----------
  test("shows error toast when RegisterUser returns error status", async () => {
    (RegisterUser as jest.Mock).mockResolvedValue({
      status: 400,
      data: { message: "Email already exists" },
    });

    render(<CreateTutorModal isOpen={true} onClose={mockOnClose} />);
    fillForm();

    fireEvent.click(screen.getByText("Submit"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Email already exists");
    });
  });

  // ---------- EXCEPTION HANDLING ----------
  test("shows error toast when RegisterUser throws exception", async () => {
    (RegisterUser as jest.Mock).mockRejectedValue(new Error("Network error"));

    render(<CreateTutorModal isOpen={true} onClose={mockOnClose} />);
    fillForm();

    fireEvent.click(screen.getByText("Submit"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Network error");
    });
  });

  // ---------- EDIT MODE ----------
  test("prefills fields and makes email read-only in edit mode", () => {
    const tutor = {
      email: "edit@tutor.com",
      firstname: "John",
      lastname: "Doe",
    };

    render(<CreateTutorModal isOpen={true} onClose={mockOnClose} tutor={tutor} />);

    expect(screen.getByDisplayValue("edit@tutor.com")).toBeInTheDocument();
    expect(screen.getByDisplayValue("John")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Doe")).toBeInTheDocument();

    const emailInput = screen.getByDisplayValue("edit@tutor.com");
    expect(emailInput).toHaveAttribute("readOnly");
    expect(screen.getByText("Update")).toBeInTheDocument();
  });

  // ---------- CANCEL ----------
  test("calls onClose when Cancel button clicked", () => {
    render(<CreateTutorModal isOpen={true} onClose={mockOnClose} />);
    fireEvent.click(screen.getByText("Cancel"));
    expect(mockOnClose).toHaveBeenCalled();
  });
});
