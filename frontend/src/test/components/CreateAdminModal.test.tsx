import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import CreateAdminModal from "@/components/CreateAdminModal";
import { RegisterUser } from "@/api/userAPI";
import { toast } from "react-toastify";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { setLoading } from "@/redux/loaderSlice";

jest.mock("@/api/userAPI", () => ({
  RegisterUser: jest.fn(),
}));
jest.mock("@/redux/store", () => ({
  useAppDispatch: jest.fn(),
  useAppSelector: jest.fn(),
}));
jest.mock("react-toastify", () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

describe("CreateAdminModal", () => {
  const mockDispatch = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useAppDispatch as jest.Mock).mockReturnValue(mockDispatch);
    (useAppSelector as jest.Mock).mockReturnValue({
      user: { permissions: ["SUPER_ADMIN"] },
    });
  });

  const fillBasicFields = () => {
    fireEvent.change(screen.getByPlaceholderText("First Name"), {
      target: { value: "Alice" },
    });
    fireEvent.change(screen.getByPlaceholderText("Last Name"), {
      target: { value: "Tan" },
    });
    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "alice@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "Pass1234" },
    });
  };

  // ---------- CLOSED MODAL ----------
  test("returns null when isOpen is false", () => {
    const { container } = render(<CreateAdminModal isOpen={false} onClose={mockOnClose} />);
    expect(container.firstChild).toBeNull();
  });

  // ---------- BASIC RENDER ----------
  test("renders all inputs and permission checkboxes when open", () => {
    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} />);
    expect(screen.getByText("Add New Admin")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("First Name")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Email")).toBeInTheDocument();
    expect(screen.getByText("Assign Permissions:")).toBeInTheDocument();

    // As SUPER_ADMIN, should see Super Admin permissions
    expect(screen.getByText("Super Admin")).toBeInTheDocument();
  });

  // ---------- REGULAR ADMIN VIEW ----------
  test("hides super-only permissions for non-super admins", () => {
    (useAppSelector as jest.Mock).mockReturnValue({ user: { permissions: [] } });
    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} />);
    expect(screen.queryByText("Super Admin")).not.toBeInTheDocument();
  });

  // ---------- VALIDATION ----------
  test("shows validation errors for required fields", async () => {
    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} />);
    fireEvent.click(screen.getByText("Create"));
    await waitFor(() => {
      expect(screen.getByText(/First name is required/)).toBeInTheDocument();
      expect(screen.getByText(/Last name is required/)).toBeInTheDocument();
      expect(screen.getByText(/Valid email is required/)).toBeInTheDocument();
      expect(screen.getByText(/Password is required/)).toBeInTheDocument();
    });
  });

  // ---------- SUCCESSFUL SUBMIT ----------
  test("calls RegisterUser and shows success toast on success", async () => {
    (RegisterUser as jest.Mock).mockResolvedValue({
      status: 200,
      data: { message: "Admin created successfully" },
    });

    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} />);
    fillBasicFields();

    // Select one permission
    const checkbox = screen.getByLabelText("View Students");
    fireEvent.click(checkbox);

    fireEvent.click(screen.getByText("Create"));

    await waitFor(() => {
      expect(mockDispatch).toHaveBeenCalledWith(setLoading(true));
      expect(RegisterUser).toHaveBeenCalledWith(
        expect.objectContaining({
          firstname: "Alice",
          lastname: "Tan",
          email: "alice@example.com",
          password: "Pass1234",
          role: "ADMIN",
        })
      );
      expect(toast.success).toHaveBeenCalledWith("Admin created successfully");
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  // ---------- FAILURE SUBMIT ----------
  test("shows error toast on failed request", async () => {
    (RegisterUser as jest.Mock).mockResolvedValue({
      status: 400,
      data: { message: "Email already exists" },
    });

    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} />);
    fillBasicFields();
    fireEvent.click(screen.getByText("Create"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Email already exists");
    });
  });

  // ---------- EXCEPTION HANDLING ----------
  test("shows error toast on thrown exception", async () => {
    (RegisterUser as jest.Mock).mockRejectedValue(new Error("Network error"));
    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} />);
    fillBasicFields();
    fireEvent.click(screen.getByText("Create"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Network error");
    });
  });

  // ---------- EDIT MODE ----------
  test("prefills fields when editing existing admin", async () => {
    const admin = { email: "bob@admin.com", permissions: ["VIEW_STUDENTS"] };
    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} admin={admin} />);

    expect(screen.getByDisplayValue("bob@admin.com")).toBeInTheDocument();
    expect(screen.getByText("Update")).toBeInTheDocument();
  });

  // ---------- CANCEL BUTTON ----------
  test("resets form and closes on Cancel", async () => {
    render(<CreateAdminModal isOpen={true} onClose={mockOnClose} />);
    fillBasicFields();
    fireEvent.click(screen.getByText("Cancel"));

    await waitFor(() => {
      expect(mockOnClose).toHaveBeenCalled();
    });
  });
});
