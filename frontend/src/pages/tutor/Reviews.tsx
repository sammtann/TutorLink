import Navbar from "@/components/Navbar";
import { useEffect, useState } from "react";
import { GetTutorReviewsByUserId } from "@/api/tutorAPI";
import { toast } from "react-toastify";
import { useAppSelector, useAppDispatch } from "@/redux/store";
import { setLoading } from "@/redux/loaderSlice";
import { StarIcon } from "@heroicons/react/24/solid";

interface Review {
  bookingId: string;
  studentName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

const Reviews = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const { user } = useAppSelector((state) => state.user);
  const dispatch = useAppDispatch();

  const fetchReviews = async () => {
    try {
      dispatch(setLoading(true));
      const token = user?.token;
      if (!token || !user?.id) return;

      const response = await GetTutorReviewsByUserId(token, user.id);
      setReviews(response.data || []);
    } catch (error: any) {
      toast.error("Failed to fetch tutor reviews");
      console.error(error);
    } finally {
      dispatch(setLoading(false));
    }
  };

  useEffect(() => {
    fetchReviews();
  }, []);

  const filteredReviews = reviews.filter(
    (r) =>
      (r.studentName?.toLowerCase() || "").includes(searchTerm.toLowerCase()) ||
      (r.comment?.toLowerCase() || "").includes(searchTerm.toLowerCase())
  );

  return (
    <div>
      <Navbar />

      <div className="p-6">
        {/* Search Bar */}
        <div className="flex justify-between items-center mb-4">
          <input
            type="search"
            placeholder="Search reviews..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full max-w-sm px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        <h2 className="text-lg font-bold mb-4">Student Reviews</h2>

        <div className="overflow-x-auto">
          {filteredReviews.length > 0 ? (
            <table className="min-w-full table-auto border-collapse">
              <thead>
                <tr className="bg-gray-100 text-left text-sm font-medium text-gray-600">
                  <th className="px-4 py-2">Student</th>
                  <th className="px-4 py-2">Rating</th>
                  <th className="px-4 py-2">Comment</th>
                  <th className="px-4 py-2">Date</th>
                </tr>
              </thead>
              <tbody>
                {filteredReviews.map((review) => (
                  <tr key={review.bookingId} className="border-b text-sm text-gray-700">
                    <td className="px-4 py-2 font-medium">{review.studentName}</td>
                    <td className="px-4 py-2 flex items-center space-x-1">
                      {[...Array(5)].map((_, idx) => (
                        <StarIcon
                          key={idx}
                          className={`h-4 w-4 ${
                            idx < review.rating ? "text-yellow-400" : "text-gray-300"
                          }`}
                        />
                      ))}
                      <span className="ml-1 text-gray-600">({review.rating})</span>
                    </td>
                    <td className="px-4 py-2 text-gray-800">{review.comment}</td>
                    <td className="px-4 py-2 text-gray-500">
                      {new Date(review.createdAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="text-gray-600 mt-4">No reviews yet.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Reviews;
