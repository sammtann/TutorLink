import { useForm } from "react-hook-form";
import { CloudIcon } from "@heroicons/react/24/solid";
import { useNavigate, Link } from "react-router-dom";
import { useAppDispatch } from "@/redux/store";
import { LoginUser } from "@/api/userAPI";
import { setLoading } from "@/redux/loaderSlice";
import { setUser } from "@/redux/userSlice";
import { toast } from "react-toastify";

const LoginAdminPage = () => {
  const {
    register,
    trigger,
    getValues,
    formState: { errors },
  } = useForm();

  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const { email, password } = getValues();
    const isValid = await trigger();
    if (!isValid) return;

    try {
      dispatch(setLoading(true));
      const response = await LoginUser(email, password);
      dispatch(setLoading(false));
      const { user } = response.data;

      if (user.role !== "ADMIN") {
        toast.error("Only administrators can log in here.");
        return;
      }

      localStorage.setItem("user", JSON.stringify(user));
      dispatch(setUser(user));
      toast.success("Welcome back, Admin!");
      navigate("/admin/dashboard");
    } catch (error: any) {
      dispatch(setLoading(false));
      toast.error(error.response?.data?.message || "Login failed");
    }
  };

  return (
    <div className="h-screen bg-primary flex items-center justify-center p-5 overflow-hidden">
      <div className="flex flex-col items-center">
        <div className="bg-white h-full w-[400px] rounded-md p-5">
          <div className="mb-5">
            <Link to={"/admin/login"}>
              <CloudIcon className="h-6 w-6 text-gray-400" />
            </Link>
            <h1 className="font-bold text-xl">Admin Login</h1>
            <p className="text-sm text-gray-500">Access the admin dashboard</p>
          </div>

          <form onSubmit={onSubmit} method="POST">
            <input
              className="bg-gray-200 px-2 py-1 rounded-md w-full"
              type="text"
              placeholder="Email"
              {...register("email", { required: true })}
            />
            {errors.email && (
              <p className="mt-1 text-red-500 text-sm">Email is required.</p>
            )}

            <input
              className="mt-3 bg-gray-200 px-2 py-1 rounded-md w-full"
              type="password"
              placeholder="Password"
              {...register("password", { required: true })}
            />
            {errors.password && (
              <p className="mt-1 text-red-500 text-sm">Password is required.</p>
            )}

            <button
              type="submit"
              className="mt-3 rounded-lg bg-primary text-white w-full px-20 py-2 transition duration-500 hover:bg-gray-200 hover:text-primary "
            >
              Login
            </button>
          </form>

          <div className="mt-3 text-sm">
            <Link className="text-primary" to="/">
              Go to student/tutor login
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginAdminPage;
