import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import BookingModalView from "@/components/BookingModalView";

describe("BookingModalView", () => {
  const mockOnClose = jest.fn();

  const booking = {
    studentName: "Alice Tan",
    tutorName: "John Tutor",
    date: new Date("2025-11-10T10:00:00Z"),
    slot: { start: "10:00", end: "11:00" },
    lessonType: "Mathematics",
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ---------- BASIC RENDER ----------
  test("renders modal with all booking details", () => {
    render(<BookingModalView booking={booking} onClose={mockOnClose} />);

    // Header
    expect(screen.getByText("Booking Details")).toBeInTheDocument();

    // Student & Tutor
    expect(screen.getByText(/Student: Alice Tan/)).toBeInTheDocument();
    expect(screen.getByText(/Tutor: John Tutor/)).toBeInTheDocument();

    // Date & time
    expect(screen.getByText(/10:00 - 11:00/)).toBeInTheDocument();
    expect(screen.getByText(/Nov/)).toBeInTheDocument(); // formatted month

    // Lesson type
    expect(screen.getByText(/Mathematics/)).toBeInTheDocument();
  });

  // ---------- CONDITIONAL RENDER ----------
  test("does not render student or tutor lines when not provided", () => {
    const partialBooking = {
      ...booking,
      studentName: "",
      tutorName: "",
    };

    render(<BookingModalView booking={partialBooking} onClose={mockOnClose} />);

    expect(screen.queryByText(/Student:/)).not.toBeInTheDocument();
    expect(screen.queryByText(/Tutor:/)).not.toBeInTheDocument();
  });

  // ---------- CLOSE BUTTON ----------
  test("calls onClose when Close button clicked", () => {
    render(<BookingModalView booking={booking} onClose={mockOnClose} />);
    fireEvent.click(screen.getByText("Close"));
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  // ---------- SNAPSHOT ----------
  test("matches snapshot", () => {
    const { container } = render(<BookingModalView booking={booking} onClose={mockOnClose} />);
    expect(container).toMatchSnapshot();
  });
});
