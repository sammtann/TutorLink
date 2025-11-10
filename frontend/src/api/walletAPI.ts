import axios, { AxiosResponse } from "axios";

const BASE_URL = `${import.meta.env.VITE_APP_API}/wallet`;

/**
 * Get wallet balance for a student
 */
export const GetWalletByUserId = async (
  studentId: string,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.get(`${BASE_URL}/${studentId}`, {
    headers: { Authorization: `Bearer ${authtoken}` },
  });
};

/**
 * Get wallet transactions for a student
 */
export const GetWalletTransactions = async (
  studentId: string,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.get(`${BASE_URL}/transactions/${studentId}`, {
    headers: { Authorization: `Bearer ${authtoken}` },
  });
};

/**
 * Manually top-up credits (used after Stripe success redirect)
 */
export const TopUpWallet = async (
  studentId: string,
  amount: number,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.post(
    `${BASE_URL}/topup`,
    { studentId, amount },
    { headers: { Authorization: `Bearer ${authtoken}` } }
  );
};

/**
 * Deduct credits for a booking
 */
export const DeductWalletCredits = async (
  studentId: string,
  amount: number,
  bookingId: string,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.post(
    `${BASE_URL}/deduct`,
    { studentId, amount, bookingId },
    { headers: { Authorization: `Bearer ${authtoken}` } }
  );
};

/**
 * Create Stripe Checkout session for top-up
 */
export const CreateCheckoutSession = async (
  studentId: string,
  amount: number,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.post(
    `${BASE_URL}/create-checkout-session`,
    { studentId, amount },
    { headers: { Authorization: `Bearer ${authtoken}` } }
  );
};

/**
 * Set wallet PIN (first-time setup)
 */
export const SetWalletPin = async (
  studentId: string,
  pin: string,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.post(
    `${BASE_URL}/set-pin`,
    { studentId, pin },
    { headers: { Authorization: `Bearer ${authtoken}` } }
  );
};

/**
 * Verify wallet PIN
 */
export const VerifyWalletPin = async (
  studentId: string,
  pin: string,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.post(
    `${BASE_URL}/verify-pin`,
    { studentId, pin },
    { headers: { Authorization: `Bearer ${authtoken}` } }
  );
};

/**
 * Withdraw all funds (mock)
 */
export const WithdrawAllFunds = async (
  studentId: string,
  pin: string,
  authtoken: string
): Promise<AxiosResponse<any>> => {
  return await axios.post(
    `${BASE_URL}/withdraw`,
    { studentId, pin },
    { headers: { Authorization: `Bearer ${authtoken}` } }
  );
};
