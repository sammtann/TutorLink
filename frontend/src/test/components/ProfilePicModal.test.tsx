import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import ProfilePicModal from "@/components/ProfilePicModal";
import { useAppSelector } from "@/redux/store";
import { toast } from "react-toastify";
import { UploadProfilePicture } from "@/api/studentAPI";
import { UploadTutorProfilePicture } from "@/api/tutorAPI";

jest.mock("@/redux/store", () => ({
  useAppSelector: jest.fn(),
}));

jest.mock("@/api/studentAPI", () => ({
  UploadProfilePicture: jest.fn(),
}));

jest.mock("@/api/tutorAPI", () => ({
  UploadTutorProfilePicture: jest.fn(),
}));

jest.mock("react-toastify", () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

describe("ProfilePicModal", () => {
  const mockOnClose = jest.fn();
  const mockRefreshProfile = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useAppSelector as jest.Mock).mockReturnValue({
      user: { id: "123", token: "mocktoken" },
    });
  });

  const renderModal = (userType: "student" | "tutor" = "student") =>
    render(
      <ProfilePicModal
        isOpen={true}
        onClose={mockOnClose}
        refreshProfile={mockRefreshProfile}
        userType={userType}
      />
    );

  test("returns null when closed", () => {
    const { container } = render(
      <ProfilePicModal
        isOpen={false}
        onClose={mockOnClose}
        refreshProfile={mockRefreshProfile}
        userType="student"
      />
    );
    expect(container.firstChild).toBeNull();
  });

  test("renders correctly when open", () => {
    renderModal();
    expect(screen.getByText("Update Profile Picture")).toBeInTheDocument();
    expect(screen.getByText("No Preview")).toBeInTheDocument();
  });

  test("shows error if no file selected or user missing", async () => {
    renderModal();
    fireEvent.click(screen.getByText("Upload"));
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Select a file or login first");
    });
  });

  test("calls onClose when Cancel clicked", () => {
    renderModal();
    fireEvent.click(screen.getByText("Cancel"));
    expect(mockOnClose).toHaveBeenCalled();
  });
});
