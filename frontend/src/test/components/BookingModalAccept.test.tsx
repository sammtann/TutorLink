import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import BookingModalAccept from "@/components/BookingModalAccept";

describe("BookingModalAccept", () => {
  const mockOnClose = jest.fn();
  const mockOnAccept = jest.fn();
  const mockOnReject = jest.fn();

  const booking = {
    studentName: "Alice Tan",
    date: new Date("2025-11-05T10:00:00Z"),
    slot: { start: "10:00", end: "11:00" },
    lessonType: "Mathematics",
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ---------- BASIC RENDER ----------
  test("renders modal with correct booking info", () => {
    render(
      <BookingModalAccept
        booking={booking}
        onClose={mockOnClose}
        onAccept={mockOnAccept}
        onReject={mockOnReject}
      />
    );

    expect(screen.getByText("Accept Booking Request")).toBeInTheDocument();
    expect(screen.getByText(/Alice Tan/)).toBeInTheDocument();
    expect(screen.getByText(/Mathematics/)).toBeInTheDocument();
    expect(screen.getByText(/10:00 - 11:00/)).toBeInTheDocument();
    expect(screen.getByText(/Nov/)).toBeInTheDocument(); // formatted date
  });

  // ---------- BUTTONS EXIST ----------
  test("renders Accept, Reject, and Close buttons", () => {
    render(
      <BookingModalAccept
        booking={booking}
        onClose={mockOnClose}
        onAccept={mockOnAccept}
        onReject={mockOnReject}
      />
    );

    expect(screen.getByText("Accept")).toBeInTheDocument();
    expect(screen.getByText("Reject")).toBeInTheDocument();
    expect(screen.getByText("Close")).toBeInTheDocument();
  });

  // ---------- ACCEPT HANDLER ----------
  test("calls onAccept when Accept button clicked", () => {
    render(
      <BookingModalAccept
        booking={booking}
        onClose={mockOnClose}
        onAccept={mockOnAccept}
        onReject={mockOnReject}
      />
    );

    fireEvent.click(screen.getByText("Accept"));
    expect(mockOnAccept).toHaveBeenCalledTimes(1);
  });

  // ---------- REJECT HANDLER ----------
  test("calls onReject when Reject button clicked", () => {
    render(
      <BookingModalAccept
        booking={booking}
        onClose={mockOnClose}
        onAccept={mockOnAccept}
        onReject={mockOnReject}
      />
    );

    fireEvent.click(screen.getByText("Reject"));
    expect(mockOnReject).toHaveBeenCalledTimes(1);
  });

  // ---------- CLOSE HANDLER ----------
  test("calls onClose when Close button clicked", () => {
    render(
      <BookingModalAccept
        booking={booking}
        onClose={mockOnClose}
        onAccept={mockOnAccept}
        onReject={mockOnReject}
      />
    );

    fireEvent.click(screen.getByText("Close"));
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  // ---------- SNAPSHOT ----------
  test("matches snapshot", () => {
    const { container } = render(
      <BookingModalAccept
        booking={booking}
        onClose={mockOnClose}
        onAccept={mockOnAccept}
        onReject={mockOnReject}
      />
    );
    expect(container).toMatchSnapshot();
  });
});
