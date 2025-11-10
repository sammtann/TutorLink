import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import ReviewModal from "@/components/ReviewModal";
import { AddTutorReview } from "@/api/tutorAPI";
import { toast } from "react-toastify";

// ----------------------
// Mocks
// ----------------------
jest.mock("@/api/tutorAPI", () => ({
  AddTutorReview: jest.fn(),
}));

jest.mock("react-toastify", () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn(),
    warning: jest.fn(),
  },
}));

describe("ReviewModal Component", () => {
  const mockOnClose = jest.fn();
  const defaultProps = {
    isOpen: true,
    onClose: mockOnClose,
    tutorId: "tutor123",
    bookingId: "booking456",
    studentName: "John Doe",
    token: "mockToken",
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("does not render when isOpen=false", () => {
    render(<ReviewModal {...defaultProps} isOpen={false} />);
    expect(screen.queryByText(/Leave a Review/i)).not.toBeInTheDocument();
  });

  test("renders correctly when open", () => {
    render(<ReviewModal {...defaultProps} />);
    expect(screen.getByText("Leave a Review")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Write your feedback...")).toBeInTheDocument();
  });

  test("shows warning if trying to submit without rating", async () => {
    render(<ReviewModal {...defaultProps} />);
    fireEvent.click(screen.getByText("Submit"));

    await waitFor(() => {
      expect(toast.warning).toHaveBeenCalledWith("Please provide a rating before submitting");
    });
    expect(AddTutorReview).not.toHaveBeenCalled();
  });

  test("updates rating and comment correctly", async () => {
    render(<ReviewModal {...defaultProps} />);
    const star = screen.getAllByText("★")[2]; // 3rd star
    fireEvent.click(star);

    const textarea = screen.getByPlaceholderText("Write your feedback...");
    fireEvent.change(textarea, { target: { value: "Great tutor!" } });

    expect(textarea).toHaveValue("Great tutor!");
    expect(star).toHaveClass("text-yellow-400");
  });

  test("submits review successfully", async () => {
    (AddTutorReview as jest.Mock).mockResolvedValueOnce({});

    render(<ReviewModal {...defaultProps} />);

    // select rating
    fireEvent.click(screen.getAllByText("★")[4]); // 5 stars
    fireEvent.change(screen.getByPlaceholderText("Write your feedback..."), {
      target: { value: "Excellent class!" },
    });

    fireEvent.click(screen.getByText("Submit"));

    await waitFor(() => {
      expect(AddTutorReview).toHaveBeenCalledWith("mockToken", "tutor123", {
        bookingId: "booking456",
        studentName: "John Doe",
        rating: 5,
        comment: "Excellent class!",
      });
      expect(toast.success).toHaveBeenCalledWith("Review submitted successfully!");
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  test("handles duplicate review warning", async () => {
    (AddTutorReview as jest.Mock).mockRejectedValueOnce({
      response: { data: { message: "already reviewed" } },
    });

    render(<ReviewModal {...defaultProps} />);

    fireEvent.click(screen.getAllByText("★")[1]);
    fireEvent.click(screen.getByText("Submit"));

    await waitFor(() => {
      expect(toast.warning).toHaveBeenCalledWith(
        "You’ve already submitted a review for this session."
      );
    });
  });

  test("handles generic error during submission", async () => {
    (AddTutorReview as jest.Mock).mockRejectedValueOnce(new Error("Network error"));

    render(<ReviewModal {...defaultProps} />);

    fireEvent.click(screen.getAllByText("★")[1]);
    fireEvent.click(screen.getByText("Submit"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Failed to submit review. Please try again.");
    });
  });

  test("calls onClose when cancel button clicked", () => {
    render(<ReviewModal {...defaultProps} />);
    fireEvent.click(screen.getByText("Cancel"));
    expect(mockOnClose).toHaveBeenCalled();
  });

  test("shows loading text while submitting", async () => {
    let resolvePromise: () => void;
    const mockPromise = new Promise<void>((resolve) => (resolvePromise = resolve));
    (AddTutorReview as jest.Mock).mockReturnValue(mockPromise);

    render(<ReviewModal {...defaultProps} />);
    fireEvent.click(screen.getAllByText("★")[3]); // 4 stars
    fireEvent.click(screen.getByText("Submit"));

    expect(screen.getByText("Submitting...")).toBeInTheDocument();

    // finish promise
    resolvePromise!();
    await waitFor(() => expect(screen.getByText("Submit")).toBeInTheDocument());
  });
});
