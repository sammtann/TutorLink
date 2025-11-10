import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { RegisterUser } from "@/api/userAPI";
import { setLoading } from "@/redux/loaderSlice";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { toast } from "react-toastify";

type Props = {
  isOpen: boolean;
  onClose: () => void;
  admin?: any;
};

const CreateAdminModal = ({ isOpen, onClose, admin }: Props) => {
  const {
    register,
    trigger,
    getValues,
    setValue,
    reset,
    formState: { errors },
  } = useForm();

  const dispatch = useAppDispatch();
  const { user } = useAppSelector((state) => state.user);

  // ✅ Define all available backend permissions
  const permissions = [
    // Student Management
    { id: "VIEW_STUDENTS", label: "View Students" },
    { id: "SUSPEND_STUDENT", label: "Suspend Student" },
    { id: "DELETE_STUDENT", label: "Delete Student" },

    // Tutor Management
    { id: "VIEW_TUTORS", label: "View Tutors" },
    { id: "APPROVE_TUTOR", label: "Approve Tutor" },
    { id: "REJECT_TUTOR", label: "Reject Tutor" },
    { id: "SUSPEND_TUTOR", label: "Suspend Tutor" },
    { id: "DELETE_TUTOR", label: "Delete Tutor" },

    // Admin Management
    { id: "VIEW_ADMIN", label: "View Admins" },
    { id: "CREATE_ADMIN", label: "Create Admins", superOnly: true },
    { id: "EDIT_ADMIN_ROLES", label: "Edit Admin Roles", superOnly: true },
    { id: "SUSPEND_ADMIN", label: "Suspend Admins", superOnly: true },
    { id: "DELETE_ADMIN", label: "Delete Admins", superOnly: true },
    { id: "SUPER_ADMIN", label: "Super Admin", superOnly: true },

    // Booking Management
    { id: "DELETE_BOOKING", label: "Delete Booking", superOnly: true },
  ];

  // ✅ Check if current user is a SUPER_ADMIN
  const isSuperAdmin = user?.permissions?.includes("SUPER_ADMIN");

  useEffect(() => {
    if (admin) {
      setValue("email", admin.email);
      if (admin.permissions) {
        setValue("permissions", admin.permissions);
      }
    } else {
      reset();
    }
  }, [admin, setValue, reset]);

  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const isValid = await trigger();
    if (!isValid) return;

    const {
      firstname,
      lastname,
      email,
      password,
      permissions: selectedPerms,
    } = getValues();

    try {
      dispatch(setLoading(true));

      const response = await RegisterUser({
        firstname,
        lastname,
        email,
        password,
        role: "ADMIN",
        permissions: selectedPerms,
      });

      dispatch(setLoading(false));

      if (response.status === 200) {
        toast.success(response.data.message);
        onClose();
      } else {
        toast.error(response.data.message);
      }
    } catch (error: any) {
      dispatch(setLoading(false));
      toast.error(error.message);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black bg-opacity-50 flex items-center justify-center">
      <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-semibold mb-4">
          {admin ? "Edit Admin" : "Add New Admin"}
        </h2>

        <form onSubmit={onSubmit}>
          {!admin && (
            <>
              <input
                className="bg-gray-100 px-3 py-2 rounded-md w-full mb-2"
                type="text"
                placeholder="First Name"
                {...register("firstname", { required: true })}
              />
              {errors.firstname && (
                <p className="text-sm text-red-500">First name is required</p>
              )}

              <input
                className="bg-gray-100 px-3 py-2 rounded-md w-full mb-2"
                type="text"
                placeholder="Last Name"
                {...register("lastname", { required: true })}
              />
              {errors.lastname && (
                <p className="text-sm text-red-500">Last name is required</p>
              )}
            </>
          )}

          <input
            className="bg-gray-100 px-3 py-2 rounded-md w-full mb-2"
            type="text"
            placeholder="Email"
            {...register("email", {
              required: true,
              pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
            })}
          />
          {errors.email && (
            <p className="text-sm text-red-500">Valid email is required</p>
          )}

          {!admin && (
            <>
              <input
                className="bg-gray-100 px-3 py-2 rounded-md w-full mb-2"
                type="password"
                placeholder="Password"
                {...register("password", { required: true })}
              />
              {errors.password && (
                <p className="text-sm text-red-500">Password is required</p>
              )}
            </>
          )}

          {/* ✅ Permission Section */}
          <div className="mt-4">
            <p className="font-medium mb-2">Assign Permissions:</p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {permissions
                .filter((perm) => isSuperAdmin || !perm.superOnly)
                .map((perm) => (
                  <label
                    key={perm.id}
                    className="flex items-center space-x-2 text-sm bg-gray-50 p-2 rounded-md border"
                  >
                    <input
                      type="checkbox"
                      value={perm.id}
                      {...register("permissions")}
                    />
                    <span>{perm.label}</span>
                  </label>
                ))}
            </div>
          </div>

          <div className="flex justify-end space-x-2 mt-6">
            <button
              type="button"
              onClick={() => {
                reset();
                onClose();
              }}
              className="px-4 py-2 rounded bg-gray-300 hover:bg-gray-400"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 rounded bg-primary text-white hover:bg-primary-dark"
            >
              {admin ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateAdminModal;
