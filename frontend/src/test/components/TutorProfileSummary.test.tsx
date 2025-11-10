import React from "react";
import { render, screen } from "@testing-library/react";
import ProfileSummary from "@/components/TutorProfileSummary";

// 🧩 Mock the AvailabilityCalendar to avoid complex rendering
jest.mock("@/components/AvailabilityCalendar", () => ({
  __esModule: true,
  default: ({ role }: { role: string }) => (
    <div data-testid="availability-calendar">Mock Calendar ({role})</div>
  ),
}));

describe("ProfileSummary Component", () => {
  const mockProfile = {
    subject: "Mathematics",
    hourlyRate: 50,
    lessonType: ["Primary", "Secondary"],
    qualifications: [
      {
        name: "B.Sc in Math Education",
        type: "Degree",
        path: "https://example.com/cert1.pdf",
        uploadedAt: "2025-01-01T00:00:00Z",
      },
      {
        name: "MOE Certified Tutor",
        type: "Certificate",
        path: "https://example.com/cert2.pdf",
      },
    ],
    description: "Experienced math tutor specializing in algebra and geometry.",
    availability: {
      Mon: { enabled: true, start: "09:00", end: "17:00" },
      Tue: { enabled: false, start: "09:00", end: "17:00" },
    },
  };

  test("renders fallback text when profile is null", () => {
    render(<ProfileSummary profile={null as any} />);
    expect(screen.getByText("No profile data.")).toBeInTheDocument();
  });

  test("renders subject, hourly rate, and description", () => {
    render(<ProfileSummary profile={mockProfile} />);
    expect(screen.getByText(/Subjects:/i)).toBeInTheDocument();
    expect(screen.getByText(/Mathematics/i)).toBeInTheDocument();
    expect(screen.getByText(/Hourly Rate:/i)).toBeInTheDocument();
    expect(screen.getByText(/SGD 50\/hr/i)).toBeInTheDocument();
    expect(screen.getByText(/Description:/i)).toBeInTheDocument();
    expect(screen.getByText(/Experienced math tutor specializing/i)).toBeInTheDocument();
  });

  test("renders lesson types correctly as list", () => {
    render(<ProfileSummary profile={mockProfile} />);
    expect(screen.getByText("Primary")).toBeInTheDocument();
    expect(screen.getByText("Secondary")).toBeInTheDocument();
  });

  test("renders qualifications with links", () => {
    render(<ProfileSummary profile={mockProfile} />);

    expect(screen.getByText("Qualifications")).toBeInTheDocument();
    expect(screen.getByText("B.Sc in Math Education")).toBeInTheDocument();
    expect(screen.getByText("MOE Certified Tutor")).toBeInTheDocument();

    const links = screen.getAllByRole("link");
    expect(links[0]).toHaveAttribute("href", "https://example.com/cert1.pdf");
    expect(links[1]).toHaveAttribute("href", "https://example.com/cert2.pdf");
  });

  test("renders uploaded date for qualifications if available", () => {
    render(<ProfileSummary profile={mockProfile} />);
    expect(screen.getByText(/Uploaded:/i)).toBeInTheDocument();
    expect(screen.getByText(/2025/i)).toBeInTheDocument();
  });

  test("renders 'No qualifications uploaded.' when empty", () => {
    const profileNoQual = { ...mockProfile, qualifications: [] };
    render(<ProfileSummary profile={profileNoQual} />);
    expect(screen.getByText("No qualifications uploaded.")).toBeInTheDocument();
  });

  test("renders 'N/A' when no lesson types provided", () => {
    const profileNoLesson = { ...mockProfile, lessonType: [] };
    render(<ProfileSummary profile={profileNoLesson} />);
    expect(screen.getByText("N/A")).toBeInTheDocument();
  });

  test("renders mock availability calendar", () => {
    render(<ProfileSummary profile={mockProfile} />);
    const calendar = screen.getByTestId("availability-calendar");
    expect(calendar).toHaveTextContent("Mock Calendar (student)");
  });

  test("renders default hourly rate placeholder when missing", () => {
    const profileNoRate = { ...mockProfile, hourlyRate: undefined };
    render(<ProfileSummary profile={profileNoRate} />);
    expect(screen.getByText(/SGD \?\?\/hr/i)).toBeInTheDocument();
  });
});
