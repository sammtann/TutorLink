import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import TutorApprovalModal from "@/components/TutorApprovalModal";

describe("TutorApprovalModal Component", () => {
  const mockOnConfirm = jest.fn();
  const mockOnCancel = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("does not render when isOpen is false", () => {
    render(
      <TutorApprovalModal
        isOpen={false}
        type="approve"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );
    expect(screen.queryByText(/Approve Tutor/i)).not.toBeInTheDocument();
  });

  test("renders approve modal correctly", () => {
    render(
      <TutorApprovalModal isOpen type="approve" onConfirm={mockOnConfirm} onCancel={mockOnCancel} />
    );

    expect(screen.getByText("Approve Tutor")).toBeInTheDocument();
    expect(screen.getByText("Are you sure you want to approve this tutor?")).toBeInTheDocument();

    // Approve button should be enabled
    const approveBtn = screen.getByText("Approve");
    expect(approveBtn).toBeEnabled();

    // Clicking approve calls onConfirm without reason
    fireEvent.click(approveBtn);
    expect(mockOnConfirm).toHaveBeenCalledWith(undefined);
  });

  test("renders reject modal with textarea", () => {
    render(
      <TutorApprovalModal isOpen type="reject" onConfirm={mockOnConfirm} onCancel={mockOnCancel} />
    );

    expect(screen.getByText("Reject Tutor")).toBeInTheDocument();
    expect(
      screen.getByText("Please provide a reason for rejecting this tutor.")
    ).toBeInTheDocument();

    const textarea = screen.getByPlaceholderText("Enter rejection reason...");
    expect(textarea).toBeInTheDocument();

    // Reject button should be initially disabled
    const rejectBtn = screen.getByText("Reject");
    expect(rejectBtn).toBeDisabled();

    // Typing a reason enables the button
    fireEvent.change(textarea, { target: { value: "Incomplete credentials" } });
    expect(rejectBtn).toBeEnabled();

    // Clicking confirm sends reason
    fireEvent.click(rejectBtn);
    expect(mockOnConfirm).toHaveBeenCalledWith("Incomplete credentials");
  });

  test("calls onCancel when cancel button is clicked", () => {
    render(
      <TutorApprovalModal isOpen type="approve" onConfirm={mockOnConfirm} onCancel={mockOnCancel} />
    );

    fireEvent.click(screen.getByText("Cancel"));
    expect(mockOnCancel).toHaveBeenCalled();
  });

  test("disables reject button again when reason cleared", () => {
    render(
      <TutorApprovalModal isOpen type="reject" onConfirm={mockOnConfirm} onCancel={mockOnCancel} />
    );

    const textarea = screen.getByPlaceholderText("Enter rejection reason...");
    const rejectBtn = screen.getByText("Reject");

    fireEvent.change(textarea, { target: { value: "Temporary suspension" } });
    expect(rejectBtn).toBeEnabled();

    fireEvent.change(textarea, { target: { value: " " } });
    expect(rejectBtn).toBeDisabled();
  });

  test("resets reason when modal closes", () => {
    const { rerender } = render(
      <TutorApprovalModal isOpen type="reject" onConfirm={mockOnConfirm} onCancel={mockOnCancel} />
    );

    const textarea = screen.getByPlaceholderText("Enter rejection reason...");
    fireEvent.change(textarea, { target: { value: "Not qualified" } });
    expect(textarea).toHaveValue("Not qualified");

    // Simulate modal closing
    rerender(
      <TutorApprovalModal
        isOpen={false}
        type="reject"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    // Modal should unmount and reset internal reason
    expect(screen.queryByPlaceholderText("Enter rejection reason...")).not.toBeInTheDocument();
  });
});
