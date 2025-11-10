import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import AvailabilityPicker from "@/components/AvailabilityPicker";
import React from "react";

interface DayAvailability {
  [day: string]: {
    start: string;
    end: string;
    enabled: boolean;
  };
}

interface AvailabilityPickerProps {
  value?: DayAvailability;
  onChange?: (availability: DayAvailability) => void;
}

describe("AvailabilityPicker Component", () => {
  const mockOnChange = jest.fn();

  const defaultAvailability: DayAvailability = {
    Mon: { start: "09:00", end: "17:00", enabled: false },
    Tue: { start: "09:00", end: "17:00", enabled: false },
    Wed: { start: "09:00", end: "17:00", enabled: false },
    Thu: { start: "09:00", end: "17:00", enabled: false },
    Fri: { start: "09:00", end: "17:00", enabled: false },
    Sat: { start: "09:00", end: "17:00", enabled: false },
    Sun: { start: "09:00", end: "17:00", enabled: false },
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders all 7 days of the week", () => {
    render(<AvailabilityPicker value={defaultAvailability} onChange={mockOnChange} />);
    const days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
    days.forEach((day) => {
      expect(screen.getByText(day)).toBeInTheDocument();
    });
  });

  test("calls onChange when availability toggles", async () => {
    render(<AvailabilityPicker value={defaultAvailability} onChange={mockOnChange} />);

    const monCheckbox = screen.getAllByRole("checkbox")[0];
    fireEvent.click(monCheckbox);

    await waitFor(() => {
      expect(mockOnChange).toHaveBeenCalled();
      const latest = mockOnChange.mock.calls[mockOnChange.mock.calls.length - 1][0];
      expect(latest.Mon.enabled).toBe(true);
    });
  });

  test("enables time inputs when checkbox is checked", async () => {
    render(<AvailabilityPicker value={defaultAvailability} onChange={mockOnChange} />);

    const monCheckbox = screen.getAllByRole("checkbox")[0];
    const timeInputs = screen.getAllByDisplayValue("09:00");
    expect(timeInputs[0]).toBeDisabled();

    fireEvent.click(monCheckbox);

    await waitFor(() => {
      expect(timeInputs[0]).not.toBeDisabled();
    });
  });

  test("updates start time for a specific day", async () => {
    render(<AvailabilityPicker value={defaultAvailability} onChange={mockOnChange} />);

    // Enable Monday
    const monCheckbox = screen.getAllByRole("checkbox")[0];
    fireEvent.click(monCheckbox);

    // Change Monday start time
    const monStartInput = screen.getAllByDisplayValue("09:00")[0];
    fireEvent.change(monStartInput, { target: { value: "10:00" } });

    await waitFor(() => {
      expect(mockOnChange).toHaveBeenCalled();
      const latest = mockOnChange.mock.calls[mockOnChange.mock.calls.length - 1][0];
      expect(latest.Mon.start).toBe("10:00");
    });
  });

  test("updates end time for a specific day", async () => {
    render(<AvailabilityPicker value={defaultAvailability} onChange={mockOnChange} />);

    // Enable Tuesday
    const tueCheckbox = screen.getAllByRole("checkbox")[1];
    fireEvent.click(tueCheckbox);

    // Change Tuesday end time
    const endInputs = screen.getAllByDisplayValue("17:00");
    fireEvent.change(endInputs[1], { target: { value: "18:00" } });

    await waitFor(() => {
      expect(mockOnChange).toHaveBeenCalled();
      const latest = mockOnChange.mock.calls[mockOnChange.mock.calls.length - 1][0];
      expect(latest.Tue.end).toBe("18:00");
    });
  });

  test("updates local state when parent value changes", async () => {
    const { rerender } = render(
      <AvailabilityPicker value={defaultAvailability} onChange={mockOnChange} />
    );

    const updatedValue = {
      ...defaultAvailability,
      Mon: { ...defaultAvailability.Mon, enabled: true },
    };

    rerender(<AvailabilityPicker value={updatedValue} onChange={mockOnChange} />);

    const monCheckbox = screen.getAllByRole("checkbox")[0];
    expect(monCheckbox).toBeChecked();
  });

  test("renders with no value prop (default internal state)", () => {
    render(<AvailabilityPicker onChange={mockOnChange} />);
    const monCheckbox = screen.getAllByRole("checkbox")[0];
    expect(monCheckbox).not.toBeChecked();
  });
});
