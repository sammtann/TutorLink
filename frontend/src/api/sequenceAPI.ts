import axios from "axios";

const BASE_URL = `${import.meta.env.VITE_APP_API}/seq`;

export const getNextStudentId = async (): Promise<string> => {
  try {
    const response = await axios.get(`${BASE_URL}/next-id`);
    return response.data;
  } catch (error) {
    console.error("Failed to fetch student number:", error);
    throw error;
  }
};
