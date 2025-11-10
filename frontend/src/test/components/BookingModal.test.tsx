import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import BookingModal, { BookingModalProps } from "@/components/BookingModal";

describe("BookingModal", () => {
  const mockOnClose = jest.fn();
  const mockOnConfirm = jest.fn();

  const baseProps: BookingModalProps = {
    lessonTypes: ["Math", "Science", "English"],
    slot: { date: new Date("2025-11-10"), slot: { start: "10:00", end: "12:00" } },
    hourlyRate: 50,
    onClose: mockOnClose,
    onConfirm: mockOnConfirm,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ---------- BASIC RENDER ----------
  it("renders lesson type dropdown and date/time info", () => {
    render(<BookingModal {...baseProps} />);
    expect(screen.getByText("Confirm Booking")).toBeInTheDocument();
    expect(screen.getByText("Math")).toBeInTheDocument();
    expect(screen.getByText("Please confirm your booking details below:")).toBeInTheDocument();
  });

  // ---------- COST CALCULATION ----------
  it("calculates estimated cost correctly (2 hours × 50 SGD/hr)", async () => {
    render(<BookingModal {...baseProps} />);
    expect(await screen.findByText("SGD 100.00")).toBeInTheDocument();
    expect(screen.getByText("(50 SGD/hr × 2 hour(s))")).toBeInTheDocument();
  });

  // ---------- OVERNIGHT SLOT ----------
  it("handles overnight slot correctly when end < start (23:00 → 01:00)", async () => {
    render(
      <BookingModal
        {...baseProps}
        slot={{ date: new Date("2025-11-10"), slot: { start: "23:00", end: "01:00" } }}
      />
    );

    // end time is next day, diff = 2h
    await waitFor(() => {
      expect(screen.getByText("SGD 100.00")).toBeInTheDocument();
    });
  });

  // ---------- ZERO-DURATION / INVALID SLOT ----------
  it("does not show estimated cost when duration ≤ 0", async () => {
    const baseProps: BookingModalProps = {
      lessonTypes: ["Math", "Science", "English"],
      slot: { date: new Date("2025-11-10"), slot: { start: "10:00", end: "10:00" } },
      hourlyRate: 50,
      onClose: mockOnClose,
      onConfirm: mockOnConfirm,
    };
    render(
      <BookingModal
        {...baseProps}
      />
    );

    await waitFor(() => {
      expect(screen.queryByText(/Estimated Cost/)).not.toBeInTheDocument();
    });
  });

  // ---------- MISSING HOURLY RATE ----------
  it("does not render estimated cost when hourlyRate is undefined", () => {
    render(<BookingModal {...baseProps} hourlyRate={undefined} />);
    expect(screen.queryByText(/Estimated Cost/)).not.toBeInTheDocument();
  });

  // ---------- LESSON TYPE CHANGE ----------
  it("updates selected lesson type when user changes selection", () => {
    render(<BookingModal {...baseProps} />);
    const select = screen.getByDisplayValue("Math");
    fireEvent.change(select, { target: { value: "English" } });
    expect(screen.getByDisplayValue("English")).toBeInTheDocument();
  });

  // ---------- CONFIRM ----------
  it("calls onConfirm with the selected lesson type", () => {
    render(<BookingModal {...baseProps} />);
    fireEvent.click(screen.getByText("Confirm"));
    expect(mockOnConfirm).toHaveBeenCalledWith("Math");
  });

  // ---------- CANCEL ----------
  it("calls onClose when Cancel button is clicked", () => {
    render(<BookingModal {...baseProps} />);
    fireEvent.click(screen.getByText("Cancel"));
    expect(mockOnClose).toHaveBeenCalled();
  });

  // ---------- EMPTY LESSON LIST ----------
  it("does not call onConfirm if no lesson types exist", () => {
    render(<BookingModal {...baseProps} lessonTypes={[]} />);
    fireEvent.click(screen.getByText("Confirm"));
    expect(mockOnConfirm).not.toHaveBeenCalled();
  });

  // ---------- SNAPSHOT ----------
  it("matches snapshot", async () => {
    const { container } = render(<BookingModal {...baseProps} />);
    await waitFor(() => screen.findByText("SGD 100.00"));
    expect(container).toMatchSnapshot();
  });
});
