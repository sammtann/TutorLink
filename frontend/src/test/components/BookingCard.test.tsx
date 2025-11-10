import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import BookingCard, { BookingCardProps } from "@/components/BookingCard";

describe("BookingCard Component", () => {
  const baseProps: BookingCardProps = {
    id: "1",
    tutorId: "T1",
    studentId: "S1",
    tutorName: "John Tutor",
    studentName: "Jane Student",
    date: "2025-11-05",
    start: "10:00",
    end: "11:00",
    lessonType: "Math Lesson",
    status: "pending",
    onClick: jest.fn(),
    onCancel: jest.fn(),
    onReschedule: jest.fn(),
    onReview: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ---------- BASIC RENDER ----------
  it("renders booking info and date correctly", () => {
    render(<BookingCard {...baseProps} />);

    expect(screen.getByText("Math Lesson")).toBeInTheDocument();
    expect(screen.getByText("John Tutor")).toBeInTheDocument();
    expect(screen.getByText("10:00 - 11:00")).toBeInTheDocument();

    const month = new Date(baseProps.date).toLocaleString("default", { month: "short" });
    expect(screen.getByText(month)).toBeInTheDocument();
  });

  // ---------- STATUS BADGES ----------
  it("renders correct status badge for each known status", () => {
    const statuses: BookingCardProps["status"][] = ["confirmed", "pending", "cancelled"];
    statuses.forEach((st) => {
      render(<BookingCard {...baseProps} status={st} />);
      const label = st.charAt(0).toUpperCase() + st.slice(1);
      expect(screen.getByText(label)).toBeInTheDocument();
    });
  });

  it("handles extended statuses with readable labels", () => {
    const extraStatuses = ["on_hold", "reschedule_requested"] as const;
    extraStatuses.forEach((st) => {
      render(<BookingCard {...baseProps} status={st as any} />);
      const expectedLabel = st
        .split("_")
        .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
        .join(" ");
      expect(screen.getByText(expectedLabel)).toBeInTheDocument();
    });
  });

  // ---------- CLICK HANDLER ----------
  it("calls onClick when clicked", () => {
    render(<BookingCard {...baseProps} />);
    fireEvent.click(screen.getByText("Math Lesson"));
    expect(baseProps.onClick).toHaveBeenCalledWith("1");
  });

  // ---------- CANCEL OVERLAY ----------
  it("shows cancel overlay on hover for pending, not for past sessions", () => {
    const { container, rerender } = render(<BookingCard {...baseProps} />);

    // Hover shows Cancel overlay
    fireEvent.mouseEnter(container.firstChild!);
    expect(screen.getByText("Cancel")).toBeInTheDocument();

    // Click Cancel
    fireEvent.click(screen.getByText("Cancel"));
    expect(baseProps.onCancel).toHaveBeenCalledWith("1");

    // Hover out hides overlay
    fireEvent.mouseLeave(container.firstChild!);
    expect(screen.queryByText("Cancel")).not.toBeInTheDocument();

    // Past sessions should not show overlay
    rerender(<BookingCard {...baseProps} isPastSession />);
    fireEvent.mouseEnter(container.firstChild!);
    expect(screen.queryByText("Cancel")).not.toBeInTheDocument();
  });

  // ---------- RESCHEDULE OVERLAY ----------
  it("shows reschedule overlay for confirmed sessions on hover", () => {
    const { container } = render(<BookingCard {...baseProps} status="confirmed" />);

    // Hover to trigger reschedule overlay
    fireEvent.mouseEnter(container.firstChild!);

    // The overlay should appear
    expect(screen.getByText("Reschedule")).toBeInTheDocument();

    // Click it
    fireEvent.click(screen.getByText("Reschedule"));
    expect(baseProps.onReschedule).toHaveBeenCalledWith(
      "1",
      "T1",
      "John Tutor",
      "Jane Student"
    );
  });

  // ---------- REVIEW BUTTON ----------
  it("shows Review button for past confirmed sessions and triggers onReview", () => {
    render(<BookingCard {...baseProps} status="confirmed" isPastSession />);

    const reviewBtn = screen.getByText("Review");
    expect(reviewBtn).toBeInTheDocument();

    fireEvent.click(reviewBtn);
    expect(baseProps.onReview).toHaveBeenCalledWith("1");
  });

  // ---------- REVIEW NOT SHOWN FOR NON-CONFIRMED ----------
  it("shows N/A for past non-confirmed sessions", () => {
    render(<BookingCard {...baseProps} status="cancelled" isPastSession />);
    expect(screen.getByText("N/A")).toBeInTheDocument();
  });

  // ---------- DASHBOARD MODE ----------
  it("does not show status or review button in dashboard mode", () => {
    render(<BookingCard {...baseProps} isDashboard />);
    expect(screen.queryByText(/Pending|Confirmed|Cancelled|Review/i)).not.toBeInTheDocument();
  });

  // ---------- HOVER STATES ----------
  it("toggles hover states only when not past session", () => {
    const { container, rerender } = render(<BookingCard {...baseProps} status="pending" />);
    fireEvent.mouseEnter(container.firstChild!);
    expect(screen.getByText("Cancel")).toBeInTheDocument();
    fireEvent.mouseLeave(container.firstChild!);
    expect(screen.queryByText("Cancel")).not.toBeInTheDocument();

    rerender(<BookingCard {...baseProps} isPastSession />);
    fireEvent.mouseEnter(container.firstChild!);
    expect(screen.queryByText("Cancel")).not.toBeInTheDocument();
  });
});
