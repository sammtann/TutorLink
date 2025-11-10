import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import TutorLessonTypeInput from "@/components/TutorLessonTypeInput";

// mock Heroicons to avoid SVG complexity
jest.mock("@heroicons/react/24/outline", () => ({
  XMarkIcon: () => <svg data-testid="xmark-icon" />,
}));

describe("TutorLessonTypeInput Component", () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders label and placeholder input", () => {
    render(<TutorLessonTypeInput value={[]} onChange={mockOnChange} />);
    expect(screen.getByText("Lesson Types")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Type or select...")).toBeInTheDocument();
  });

  test("shows existing lesson tags with remove button", () => {
    render(<TutorLessonTypeInput value={["Math", "Science"]} onChange={mockOnChange} />);
    expect(screen.getByText("Math")).toBeInTheDocument();
    expect(screen.getByText("Science")).toBeInTheDocument();

    const removeBtns = screen.getAllByRole("button");
    expect(removeBtns.length).toBe(2);
  });

  test("calls onChange when removing a lesson", () => {
    render(<TutorLessonTypeInput value={["English"]} onChange={mockOnChange} />);
    fireEvent.click(screen.getByRole("button"));
    expect(mockOnChange).toHaveBeenCalledWith([]);
  });

  test("adds lesson when pressing Enter", () => {
    render(<TutorLessonTypeInput value={[]} onChange={mockOnChange} />);

    const input = screen.getByPlaceholderText("Type or select...");
    fireEvent.change(input, { target: { value: "Piano" } });
    fireEvent.keyDown(input, { key: "Enter" });

    expect(mockOnChange).toHaveBeenCalledWith(["Piano"]);
  });

  test("prevents adding duplicate lessons", () => {
    render(<TutorLessonTypeInput value={["Coding"]} onChange={mockOnChange} />);
    const input = screen.getByPlaceholderText("Type or select...");
    fireEvent.change(input, { target: { value: "Coding" } });
    fireEvent.keyDown(input, { key: "Enter" });

    expect(mockOnChange).not.toHaveBeenCalled();
  });

  test("filters suggestions based on input", () => {
    render(
      <TutorLessonTypeInput
        value={[]}
        onChange={mockOnChange}
        suggestions={["Math", "Science", "Coding"]}
      />
    );

    const input = screen.getByPlaceholderText("Type or select...");
    fireEvent.change(input, { target: { value: "co" } });

    const suggestions = screen.getAllByRole("listitem");
    expect(suggestions).toHaveLength(1);
    expect(suggestions[0]).toHaveTextContent("Coding");
  });

  test("clicking a suggestion adds it to list and hides dropdown", () => {
    render(
      <TutorLessonTypeInput value={[]} onChange={mockOnChange} suggestions={["Math", "Science"]} />
    );

    const input = screen.getByPlaceholderText("Type or select...");
    fireEvent.change(input, { target: { value: "sci" } });

    const suggestion = screen.getByText("Science");
    fireEvent.click(suggestion);

    expect(mockOnChange).toHaveBeenCalledWith(["Science"]);
    expect(screen.queryByText("Science")).not.toBeInTheDocument(); // dropdown closed
  });

  test("does not render suggestions when input empty", () => {
    render(
      <TutorLessonTypeInput value={[]} onChange={mockOnChange} suggestions={["Art", "Music"]} />
    );

    expect(screen.queryByRole("listitem")).not.toBeInTheDocument();
  });

  test("handles Enter key only when input is not empty", () => {
    render(<TutorLessonTypeInput value={[]} onChange={mockOnChange} />);
    const input = screen.getByPlaceholderText("Type or select...");
    fireEvent.keyDown(input, { key: "Enter" });
    expect(mockOnChange).not.toHaveBeenCalled();
  });

  test("renders multiple tags with icons", () => {
    render(<TutorLessonTypeInput value={["Math", "Coding"]} onChange={mockOnChange} />);
    expect(screen.getAllByTestId("xmark-icon")).toHaveLength(2);
  });
});
