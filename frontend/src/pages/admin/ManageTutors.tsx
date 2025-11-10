import Navbar from "@/components/Navbar";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { EyeIcon, PauseCircleIcon, PlayCircleIcon, TrashIcon } from "@heroicons/react/24/solid";
import { GetAllTutors, DeleteUser, ActivateUser, SuspendUser} from "@/api/adminAPI";
import { toast } from "react-toastify";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { Tutor } from "@/types/TutorType";
import { setLoading } from "@/redux/loaderSlice";

const ManageTutors = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState("");
  const [tutors, setTutors] = useState<Tutor[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const tutorsPerPage = 10;
  const dispatch = useAppDispatch();

  // Sorting
  const [sortConfig, setSortConfig] = useState<{ key: keyof Tutor; direction: "asc" | "desc" } | null>(null);
  const { user } = useAppSelector((state) => state.user);
  const currentPermissions: string[] = user?.permissions || [];

  const fetchTutors = async () => {
    try {
      dispatch(setLoading(true));
      const token = user?.token;
      if (!token) return;
      const response = await GetAllTutors(user.id, token);
      setTutors(response.data);
    } catch (error: any) {
      toast.error("Failed to fetch tutors");
      console.error(error);
    }
    finally {
      dispatch(setLoading(false));
    }
  };

  const handleViewTutor = (tutor: Tutor) => {
    navigate(`/admin/tutors/${tutor.userId}`);
  };

  const handleSuspendModal = async (tutor: any) => {
    const isSuspended = tutor.status === "SUSPENDED";
    const action = isSuspended ? "activate" : "suspend";

    if (!confirm(`Are you sure you want to ${action} this tutor?`)) return;

    try {
      const token = user?.token;
      if (!token) return;

      if (isSuspended) {
        // tutor is suspended → call activate API
        await ActivateUser(user?.id, tutor.userId, token, "TUTOR");
        toast.success("Tutor activated successfully");
      } else {
        // tutor is active → call suspend API
        await SuspendUser(user?.id, tutor.userId, token, "TUTOR");
        toast.success("Tutor suspended successfully");
      }

      fetchTutors(); // refresh list
    } catch (error: any) {
      toast.error(`Failed to ${action} tutor`);
      console.error(error);
    }
  };

  const handleDelete = async (tutorId: any) => {
    if (!confirm("Are you sure you want to delete this tutor?")) return;

    try {
      const token = user?.token;
      if (!token) return;

      await DeleteUser(user.id, tutorId, token, "TUTOR");
      toast.success("Tutor deleted successfully");
      fetchTutors(); // Refresh the list
    } catch (error: any) {
      toast.error("Failed to delete tutor");
      console.error(error);
    }
  };

  // Filter tutors by search
  const filteredTutors = tutors.filter((tutor: Tutor) => {
    const term = searchTerm.toLowerCase();

    return (
      tutor.firstName.toLowerCase().includes(term) ||
      tutor.lastName.toLowerCase().includes(term) ||
      tutor.email.toLowerCase().includes(term) ||
      tutor.status.toLowerCase().includes(term)
    );
  });

  // Sort tutors
  const sortedTutors = [...filteredTutors].sort((a: any, b: any) => {
    if (!sortConfig) return 0;
    const { key, direction } = sortConfig;
    const order = direction === "asc" ? 1 : -1;
    return a[key] > b[key] ? order : a[key] < b[key] ? -order : 0;
  });

  // Pagination
  const indexOfLastTutor = currentPage * tutorsPerPage;
  const indexOfFirstTutor = indexOfLastTutor - tutorsPerPage;
  const currentTutors = sortedTutors.slice(indexOfFirstTutor, indexOfLastTutor);
  const totalPages = Math.ceil(sortedTutors.length / tutorsPerPage);

  const handlePageChange = (page: number) => {
    if (page > 0 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const handleSort = (key: keyof Tutor) => {
    if (sortConfig?.key === key) {
      setSortConfig({ key, direction: sortConfig.direction === "asc" ? "desc" : "asc" });
    } else {
      setSortConfig({ key, direction: "asc" });
    }
  };

  useEffect(() => {
    fetchTutors();
  }, []);

  return (
    <div>
      <Navbar />

      <div className="p-6">
        {/* Search Bar */}
        <input
          type="search"
          placeholder="Search tutors..."
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setCurrentPage(1);
          }}
          className="w-full max-w-sm px-2 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
        />

        <div className="flex justify-between items-center my-4">
          <h2 className="text-lg font-bold">List of Tutors</h2>
        </div>

        <div className="ml-4 mr-4 overflow-x-auto">
          <table className="min-w-full table-fixed border-collapse">
            <thead>
              <tr className="bg-gray-100 text-left text-sm font-medium text-gray-600">
                <th className="w-16 px-2 py-2 w-1/12">S/N</th>

                {/* Name Column */}
                <th
                  onClick={() => handleSort("firstName" as keyof Tutor)}
                  className="px-2 py-2 w-1/6 cursor-pointer"
                >
                  <div className="flex items-center space-x-1">
                    <span>Name</span>
                    <span className="w-3 text-xs">
                      {sortConfig?.key === ("firstName" as keyof Tutor) &&
                        (sortConfig.direction === "asc" ? "▲" : "▼")}
                    </span>
                  </div>
                </th>

                {/* Email Column */}
                <th
                  onClick={() => handleSort("email" as keyof Tutor)}
                  className="px-2 py-2 w-1/4 cursor-pointer"
                >
                  <div className="flex items-center space-x-1">
                    <span>Email</span>
                    <span className="w-3 text-xs">
                      {sortConfig?.key === ("email" as keyof Tutor) &&
                        (sortConfig.direction === "asc" ? "▲" : "▼")}
                    </span>
                  </div>
                </th>

                {/* Status Column */}
                <th
                  onClick={() => handleSort("status" as keyof Tutor)}
                  className="px-2 py-2 w-1/6 cursor-pointer"
                >
                  <div className="flex items-center space-x-1">
                    <span>Status</span>
                    <span className="w-3 text-xs">
                      {sortConfig?.key === ("status" as keyof Tutor) &&
                        (sortConfig.direction === "asc" ? "▲" : "▼")}
                    </span>
                  </div>
                </th>

                <th className="px-2 py-2 w-1/4">Action</th>
              </tr>
            </thead>
            <tbody>
              {currentTutors.map((tutor, idx) => (
                <tr key={tutor.userId} className="border-b text-sm text-gray-700">
                  <td className="px-2 py-2">{indexOfFirstTutor + idx + 1}</td>
                  <td className="px-2 py-2">{`${tutor.firstName} ${tutor.lastName}`}</td>
                  <td className="px-2 py-2">{tutor.email}</td>
                  <td
                    className={`px-2 py-2 ${tutor.status === "ACTIVE"
                      ? "text-green-600"
                      : tutor.status === "SUSPENDED"
                        ? "text-orange-600"
                        : tutor.status === "PENDING_APPROVAL"
                          ? "text-blue-600"
                          : tutor.status === "REJECTED"
                            ? "text-red-600"
                            : "text-gray-600"
                      }`}
                  >
                    {tutor.status}
                  </td>
                  <td className="px-2 py-2 space-x-2">

                    <button
                      onClick={() => handleViewTutor(tutor)}
                      className="bg-blue-100 text-blue-700 px-3 py-1 rounded-md backdrop-blur-sm hover:bg-blue-200 transition inline-flex items-center space-x-1">
                      <EyeIcon className="h-4 w-4" />
                      <span>View</span>
                    </button>
                    <div className="relative group inline-block">
                      {/* Suspend/Activate Button */}
                      <button
                        onClick={() => currentPermissions.includes("SUSPEND_TUTOR") && handleSuspendModal(tutor)}
                        disabled={!currentPermissions.includes("SUSPEND_TUTOR") || tutor.status === "DELETED" || tutor.status === "REJECTED"}
                        className={`px-3 py-1 rounded-md backdrop-blur-sm transition inline-flex items-center space-x-1
                          ${!currentPermissions.includes("SUSPEND_TUTOR") || tutor.status === "DELETED" || tutor.status === "REJECTED"
                            ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                            : tutor.status === "DELETED" || tutor.status === "REJECTED"
                              ? "bg-green-100 text-green-700 hover:bg-green-200"
                              : "bg-orange-100 text-orange-700 hover:bg-orange-200"
                          }`}
                      >
                        {tutor.status === "SUSPENDED" ? (
                          <PlayCircleIcon className="h-4 w-4" />
                        ) : (
                          <PauseCircleIcon className="h-4 w-4" />
                        )}
                        <span>{tutor.status === "SUSPENDED" ? "Activate" : "Suspend"}</span>
                      </button>

                      {/* Tooltip if no permission */}
                      {!currentPermissions.includes("SUSPEND_TUTOR") && (
                        <div className="absolute -top-8 left-1/2 -translate-x-1/2 whitespace-nowrap 
                    bg-gray-700 text-white text-xs px-2 py-2 rounded opacity-0 
                    group-hover:opacity-100 transition">
                          You do not have permission to suspend tutors
                        </div>
                      )}
                      {tutor.status === "DELETED" || tutor.status === "REJECTED" && (
                        <div className="absolute -top-8 left-1/2 -translate-x-1/2 whitespace-nowrap 
                    bg-gray-700 text-white text-xs px-2 py-2 rounded opacity-0 
                    group-hover:opacity-100 transition">
                          You can only suspend active tutors
                        </div>
                      )}
                    </div>

                    <div className="relative group inline-block">
                      <button
                        onClick={() => currentPermissions.includes("DELETE_TUTOR") && handleDelete(tutor.userId)}
                        disabled={!currentPermissions.includes("DELETE_TUTOR") || (tutor.status !== "SUSPENDED" && tutor.status !== "REJECTED")}
                        className={`px-3 py-1 rounded-md backdrop-blur-sm transition inline-flex items-center space-x-1
                          ${!currentPermissions.includes("DELETE_TUTOR")
                            ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                            : (tutor.status !== "SUSPENDED" && tutor.status !== "REJECTED")
                              ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                              : "bg-red-100 text-red-700 hover:bg-red-200"
                          }`}
                      >
                        <TrashIcon className="h-4 w-4" />
                        <span>Delete</span>
                      </button>

                      {/* Tooltip */}
                      {!currentPermissions.includes("DELETE_TUTOR") ? (
                        <div className="absolute -top-8 left-1/2 -translate-x-1/2 whitespace-nowrap 
                    bg-gray-700 text-white text-xs px-2 py-2 rounded opacity-0 
                    group-hover:opacity-100 transition">
                          You do not have permission to delete tutors
                        </div>
                      ) : tutor.status === "ACTIVE" || tutor.status === "PENDING_APPROVAL" ? (
                        <div className="absolute -top-8 left-1/2 -translate-x-1/2 whitespace-nowrap 
                    bg-gray-700 text-white text-xs px-2 py-2 rounded opacity-0 
                    group-hover:opacity-100 transition">
                          You cannot delete an active/pending tutor
                        </div>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination controls */}
        {totalPages > 1 && (
          <div className="flex justify-center items-center mt-4 space-x-2">
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
              className="px-3 py-1 border rounded-md disabled:opacity-50"
            >
              Prev
            </button>
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i + 1}
                onClick={() => handlePageChange(i + 1)}
                className={`px-3 py-1 border rounded-md ${currentPage === i + 1 ? "bg-blue-500 text-white" : ""
                  }`}
              >
                {i + 1}
              </button>
            ))}
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="px-3 py-1 border rounded-md disabled:opacity-50"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ManageTutors;
