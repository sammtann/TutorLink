import Navbar from "@/components/Navbar";
import { useEffect, useState } from "react";
import { TrashIcon, EyeIcon, PauseCircleIcon, PlayCircleIcon, PlusCircleIcon } from "@heroicons/react/24/solid";
import { GetAllAdmins, DeleteUser, ActivateUser, SuspendUser } from "@/api/adminAPI";
import { toast } from "react-toastify";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { setLoading } from "@/redux/loaderSlice";
import { useNavigate } from "react-router-dom";
import CreateAdminModal from "@/components/CreateAdminModal";

const ManageAdmins = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [admins, setAdmins] = useState<AdminDetails[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { user } = useAppSelector((state) => state.user);
  const currentPermissions: string[] = user?.permissions || [];

  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const fetchAdmins = async () => {
    try {
      dispatch(setLoading(true));
      const token = user?.token;
      if (!token) return;

      const response = await GetAllAdmins(user.id, token);
      setAdmins(response.data);
    } catch (error: any) {
      toast.error("Failed to fetch admins");
      console.error(error);
    } finally {
      dispatch(setLoading(false));
    }
  };

  const filteredAdmins = admins.filter((admin: any) =>
    admin.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleViewAdmin = (admin: any | null = null) => {
    navigate(`/admin/admins/${admin.id}`);
  };

  const handleSuspendModal = async (admin: any) => {
    const isSuspended = admin.status === "SUSPENDED";
    const action = isSuspended ? "activate" : "suspend";

    if (!confirm(`Are you sure you want to ${action} this admin?`)) return;

    try {
      const token = user?.token;
      if (!token) return;

      if (isSuspended) {
        await ActivateUser(user?.id, admin.id, token, "ADMIN");
        toast.success("Admin activated successfully");
      } else {
        await SuspendUser(user?.id, admin.id, token, "ADMIN");
        toast.success("Admin suspended successfully");
      }

      fetchAdmins();
    } catch (error: any) {
      toast.error(`Failed to ${action} admin`);
      console.error(error);
    }
  };

  const handleDelete = async (adminId: number) => {
    if (!confirm("Are you sure you want to delete this admin?")) return;

    try {
      const token = user?.token;
      if (!token) return;

      await DeleteUser(user.id, adminId, token, "ADMIN");
      toast.success("Admin deleted successfully");
      fetchAdmins();
    } catch (error: any) {
      toast.error("Failed to delete admin");
      console.error(error);
    }
  };

  useEffect(() => {
    fetchAdmins();
  }, []);

  return (
    <div>
      <Navbar />
      <div className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-bold">List of Admins</h2>

          {/* ✅ Show Create Admin button only if user has CREATE_ADMIN */}
          {currentPermissions.includes("CREATE_ADMIN") && (
            <button
              onClick={() => setIsModalOpen(true)}
              className="flex items-center space-x-2 px-4 py-2 rounded-md bg-primary text-white hover:bg-primary-dark transition"
            >
              <PlusCircleIcon className="h-5 w-5" />
              <span>Create Admin</span>
            </button>
          )}
        </div>

        {/* Search Bar */}
        <input
          type="search"
          placeholder="Search admins..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full max-w-sm px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
        />

        {/* Table */}
        <div className="overflow-x-auto mt-4">
          <table className="min-w-full table-auto border-collapse">
            <thead>
              <tr className="bg-gray-100 text-left text-sm font-medium text-gray-600">
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">Email</th>
                <th className="px-4 py-2">Status</th>
                <th className="px-4 py-2">Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredAdmins.map((admin: any) => (
                <tr key={admin.id} className="border-b text-sm text-gray-700">
                  <td className="px-4 py-2">{admin.name}</td>
                  <td className="px-4 py-2">{admin.email}</td>
                  <td
                    className={`px-4 py-2 ${admin.status === "ACTIVE"
                      ? "text-green-600"
                      : admin.status === "SUSPENDED"
                        ? "text-red-600"
                        : "text-gray-600"
                      }`}
                  >
                    {admin.status}
                  </td>
                  <td className="px-4 py-2 space-x-2">
                    <button
                      onClick={() => handleViewAdmin(admin)}
                      className="bg-blue-100 text-blue-700 px-3 py-1 rounded-md hover:bg-blue-200 transition inline-flex items-center space-x-1"
                    >
                      <EyeIcon className="h-4 w-4" />
                      <span>View</span>
                    </button>

                    {/* Suspend / Activate Button */}
                    <button
                      onClick={() =>
                        currentPermissions.includes("SUSPEND_ADMIN") &&
                        handleSuspendModal(admin)
                      }
                      disabled={
                        !currentPermissions.includes("SUSPEND_ADMIN") ||
                        admin.id === user?.id ||
                        admin.status === "DELETED"
                      }
                      className={`px-3 py-1 rounded-md transition inline-flex items-center space-x-1
                        ${!currentPermissions.includes("SUSPEND_ADMIN") ||
                        admin.id === user?.id ||
                        admin.status === "DELETED"
                          ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                          : admin.status === "SUSPENDED"
                            ? "bg-green-100 text-green-700 hover:bg-green-200"
                            : "bg-orange-100 text-orange-700 hover:bg-orange-200"
                        }`}
                    >
                      {admin.status === "SUSPENDED" ? (
                        <PlayCircleIcon className="h-4 w-4" />
                      ) : (
                        <PauseCircleIcon className="h-4 w-4" />
                      )}
                      <span>
                        {admin.status === "SUSPENDED" ? "Activate" : "Suspend"}
                      </span>
                    </button>

                    {/* Delete Button */}
                    <button
                      onClick={() =>
                        currentPermissions.includes("DELETE_ADMIN") &&
                        handleDelete(admin.id)
                      }
                      disabled={
                        !currentPermissions.includes("DELETE_ADMIN") ||
                        admin.status !== "SUSPENDED"
                      }
                      className={`px-3 py-1 rounded-md transition inline-flex items-center space-x-1
                        ${!currentPermissions.includes("DELETE_ADMIN") ||
                        admin.status !== "SUSPENDED"
                          ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                          : "bg-red-100 text-red-700 hover:bg-red-200"
                        }`}
                    >
                      <TrashIcon className="h-4 w-4" />
                      <span>Delete</span>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* ✅ Create Admin Modal */}
        <CreateAdminModal
          isOpen={isModalOpen}
          onClose={() => {
            setIsModalOpen(false);
            fetchAdmins(); // Refresh list after close
          }}
        />
      </div>
    </div>
  );
};

export default ManageAdmins;
