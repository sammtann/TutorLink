import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAppSelector } from "@/redux/store";
import Navbar from "@/components/Navbar";
import { toast } from "react-toastify";
import {
  GetWalletByUserId,
  GetWalletTransactions,
  SetWalletPin,
  WithdrawAllFunds,
} from "@/api/walletAPI";

const TutorWalletPage = () => {
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.user);

  const [balance, setBalance] = useState<number>(0);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Withdraw modal state
  const [showPinModal, setShowPinModal] = useState(false);
  const [isPinSet, setIsPinSet] = useState(false);
  const [pinInput, setPinInput] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);

  // --------------------------
  // Fetch wallet details
  // --------------------------
  const fetchWallet = async () => {
    if (!user?.id || !user?.token) {
      toast.error("User not logged in");
      navigate("/");
      return;
    }

    try {
      const [walletRes, txnRes] = await Promise.all([
        GetWalletByUserId(user.id, user.token),
        GetWalletTransactions(user.id, user.token),
      ]);

      setBalance(walletRes.data.balance ?? 0);
      setTransactions(txnRes.data ?? []);
      setIsPinSet(walletRes.data.pinSet ?? false);
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Failed to fetch wallet details");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // --------------------------
  // Withdraw logic
  // --------------------------
  const handleWithdrawClick = () => {
    if (!user?.id || !user?.token) {
      toast.error("User not logged in");
      navigate("/");
      return;
    }
    setShowPinModal(true);
  };

  const handlePinSubmit = async () => {
    if (!pinInput || pinInput.length < 4) {
      toast.error("PIN must be at least 4 digits");
      return;
    }
    if (!user?.id || !user?.token) {
      toast.error("User not logged in");
      navigate("/");
      return;
    }

    setIsProcessing(true);
    try {
      if (!isPinSet) {
        // First-time setup
        await SetWalletPin(user.id, pinInput, user.token);
        toast.success("Wallet PIN set successfully");
        setIsPinSet(true);
      } else {
        // Withdraw all funds (mock)
        const res = await WithdrawAllFunds(user.id, pinInput, user.token);
        toast.success(res.data.message || "Withdrawal successful");
        await fetchWallet();
      }
      setShowPinModal(false);
      setPinInput("");
    } catch (err: any) {
      toast.error(err?.response?.data?.error || "Failed to process withdrawal");
      console.error(err);
    } finally {
      setIsProcessing(false);
    }
  };

  useEffect(() => {
    fetchWallet();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="min-h-screen bg-[#f2f2f2]">
      <Navbar />
      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="flex justify-between items-center mb-5">
          <div className="flex items-center">
            <button
              onClick={() => navigate(-1)}
              className="mr-4 px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition">
              ← Back
            </button>
            <h1 className="font-bold text-xl">My Wallet</h1>
          </div>
        </div>

        {loading ? (
          <div className="text-center text-gray-500 py-20 text-lg">Loading wallet...</div>
        ) : (
          <>
            {/* Balance Card */}
            <div className="bg-white p-6 rounded-xl shadow-md mb-6 flex justify-between items-center">
              <div>
                <h2 className="text-lg font-semibold text-gray-600">Current Balance</h2>
                <p className="text-3xl font-bold text-blue-600 mt-1">{balance.toFixed(2)} SGD</p>
              </div>
              <div className="flex flex-col items-end text-right gap-3">
                <div className="text-gray-500 text-sm">
                  <p>Funds reflect session payouts and platform adjustments.</p>
                  <p className="italic">(Top-ups disabled for tutors)</p>
                </div>

                {/* Withdraw Button */}
                <button
                  onClick={handleWithdrawClick}
                  className="px-5 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition">
                  Withdraw All Funds
                </button>
              </div>
            </div>

            {/* Transaction History */}
            <div className="bg-white p-6 rounded-xl shadow-md">
              <h2 className="text-lg font-semibold mb-4">Transaction History</h2>
              {transactions.length === 0 ? (
                <div className="text-gray-400 text-center py-10">No transactions recorded yet.</div>
              ) : (
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="border-b text-left text-gray-600">
                      <th className="pb-2">Date</th>
                      <th className="pb-2">Type</th>
                      <th className="pb-2">Description</th>
                      <th className="pb-2 text-right">Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((txn) => (
                      <tr key={txn.id} className="border-b last:border-none">
                        <td className="py-2">
                          {new Date(txn.createdAt || txn.date).toLocaleString()}
                        </td>
                        <td className="py-2">{txn.type}</td>
                        <td className="py-2">{txn.description}</td>
                        <td
                          className={`py-2 text-right font-semibold ${
                            txn.amount > 0 ? "text-green-600" : "text-red-500"
                          }`}>
                          {txn.amount > 0 ? "+" : ""}
                          {txn.amount} SGD
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </>
        )}
      </div>

      {/*  Wallet PIN Modal */}
      {showPinModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-lg p-6 w-80 text-center">
            <h2 className="text-lg font-semibold mb-4">
              {isPinSet ? "Enter Wallet PIN" : "Set Wallet PIN"}
            </h2>

            <input
              type="password"
              maxLength={6}
              placeholder="Enter 4-6 digit PIN"
              value={pinInput}
              onChange={(e) => setPinInput(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 mb-4 text-center"
            />

            <div className="flex justify-center gap-3">
              <button
                disabled={isProcessing}
                onClick={handlePinSubmit}
                className={`px-4 py-2 ${
                  isPinSet ? "bg-green-600 hover:bg-green-700" : "bg-blue-600 hover:bg-blue-700"
                } text-white rounded-md transition`}>
                {isProcessing ? "Processing..." : isPinSet ? "Confirm Withdraw" : "Set PIN"}
              </button>

              <button
                onClick={() => {
                  setShowPinModal(false);
                  setPinInput("");
                }}
                className="px-4 py-2 bg-gray-300 rounded-md hover:bg-gray-400 transition">
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TutorWalletPage;
