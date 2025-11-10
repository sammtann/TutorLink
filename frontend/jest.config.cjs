// jest.config.cjs
module.exports = {
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.(t|j)sx?$": "babel-jest", // Babel handles TS + JSX
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",

    // ✅ Mock CSS imports
    "\\.(css|less|scss|sass)$": "identity-obj-proxy",

    // ✅ Mock all image and static asset imports
    "\\.(jpg|jpeg|png|gif|svg)$": "<rootDir>/src/__mocks__/fileMock.js",
  },
  setupFilesAfterEnv: ["<rootDir>/src/setupTests.ts"],
  testPathIgnorePatterns: ["/node_modules/", "/dist/"],
  moduleFileExtensions: ["ts", "tsx", "js", "jsx"],
};
