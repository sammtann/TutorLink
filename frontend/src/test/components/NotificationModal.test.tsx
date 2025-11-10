import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import NotificationsModal from "@/components/NotificationsModal";

describe("NotificationsModal", () => {
  const mockOnClose = jest.fn();
  const mockOnClick = jest.fn();

  const mockNotifications = [
    {
      id: "1",
      message: "New booking request",
      read: false,
      userId: "u1",
      type: "BOOKING",
      bookingId: "b1",
      createdAt: new Date().toISOString(),
      onClick: mockOnClick,
    },
    {
      id: "2",
      message: "Payment received",
      read: true,
      userId: "u1",
      type: "PAYMENT",
      bookingId: "b2",
      createdAt: new Date().toISOString(),
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ---------- CLOSED ----------
  test("returns null when isOpen is false", () => {
    const { container } = render(
      <NotificationsModal isOpen={false} onClose={mockOnClose} notifications={mockNotifications} />
    );
    expect(container.firstChild).toBeNull();
  });

  // ---------- OPEN ----------
  test("renders modal with notifications list", () => {
    render(
      <NotificationsModal isOpen={true} onClose={mockOnClose} notifications={mockNotifications} />
    );

    expect(screen.getByText("Notifications")).toBeInTheDocument();
    expect(screen.getByText("New booking request")).toBeInTheDocument();
    expect(screen.getByText("Payment received")).toBeInTheDocument();
  });

  // ---------- NO NOTIFICATIONS ----------
  test("shows 'No notifications' message when list is empty", () => {
    render(<NotificationsModal isOpen={true} onClose={mockOnClose} notifications={[]} />);
    expect(screen.getByText("No notifications")).toBeInTheDocument();
  });

  // ---------- CLOSE BUTTON ----------
  test("calls onClose when ✕ button clicked", () => {
    render(
      <NotificationsModal isOpen={true} onClose={mockOnClose} notifications={mockNotifications} />
    );
    fireEvent.click(screen.getByText("✕"));
    expect(mockOnClose).toHaveBeenCalled();
  });

  // ---------- CLICK NOTIFICATION ----------
  test("calls notification onClick when notification clicked", () => {
    render(
      <NotificationsModal isOpen={true} onClose={mockOnClose} notifications={mockNotifications} />
    );

    const firstNotification = screen.getByText("New booking request");
    fireEvent.click(firstNotification);
    expect(mockOnClick).toHaveBeenCalled();
  });

  // ---------- READ / UNREAD STYLING ----------
  test("renders unread and read notifications with correct classnames", () => {
    render(
      <NotificationsModal isOpen={true} onClose={mockOnClose} notifications={mockNotifications} />
    );

    const unread = screen.getByText("New booking request");
    const read = screen.getByText("Payment received");

    expect(unread.className).toMatch(/bg-white/);
    expect(read.className).toMatch(/bg-gray-100/);
  });
});
