import React from "react";
import { render, screen } from "@testing-library/react";
import RingChart from "@/components/RingChart";

// 🧩 Mock recharts to avoid actual SVG rendering
jest.mock("recharts", () => {
  const MockPieChart = ({ children }: any) => <div data-testid="PieChart">{children}</div>;
  const MockPie = ({ children }: any) => <div data-testid="Pie">{children}</div>;
  const MockCell = ({ fill }: any) => <div data-testid="Cell" style={{ backgroundColor: fill }} />;
  const MockResponsiveContainer = ({ children }: any) => (
    <div data-testid="ResponsiveContainer">{children}</div>
  );
  return {
    PieChart: MockPieChart,
    Pie: MockPie,
    Cell: MockCell,
    ResponsiveContainer: MockResponsiveContainer,
  };
});

describe("RingChart Component", () => {
  test("renders pending and unverified if provided", () => {
    render(
      <RingChart
        title="Student Accounts"
        total={200}
        active={100}
        suspended={50}
        pending={30}
        unverified={20}
      />
    );

    expect(screen.getByText("Student Accounts")).toBeInTheDocument();
    expect(screen.getByText("50.0%")).toBeInTheDocument();

    // Legend should include all non-zero values
    expect(screen.getByText(/Pending: 30/i)).toBeInTheDocument();
    expect(screen.getByText(/Unverified: 20/i)).toBeInTheDocument();
  });

  test("renders 0% when total is 0", () => {
    render(<RingChart title="Empty Chart" total={0} active={0} suspended={0} />);
    expect(screen.getByText("0%")).toBeInTheDocument();
  });

  test("renders single-column legend layout when only one category > 0", () => {
    render(<RingChart title="Only Active" total={10} active={10} suspended={0} />);
    const legendContainer = screen.getByText(/Active: 10/i).closest("div.grid");
    expect(legendContainer).toHaveClass("grid-cols-1");
  });

  test("renders two-column legend layout when multiple categories > 0", () => {
    render(<RingChart title="Multiple" total={10} active={5} suspended={5} />);
    const legendContainer = screen.getByText(/Active: 5/i).closest("div.grid");
    expect(legendContainer).toHaveClass("grid-cols-2");
  });
});
