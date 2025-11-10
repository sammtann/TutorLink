import "@testing-library/jest-dom";

// 🧩 Mock Vite import.meta.env for Jest so modules using it don't crash
Object.defineProperty(globalThis, "import", {
  value: {
    meta: {
      env: {
        VITE_APP_API: "http://localhost:8080",
        VITE_APP_STRIPE_KEY: "pk_test_123",
        MODE: "test",
      },
    },
  },
});
