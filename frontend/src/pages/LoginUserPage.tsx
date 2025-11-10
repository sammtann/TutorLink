import { useForm } from "react-hook-form";
import { useNavigate, Link } from "react-router-dom";
import { useAppDispatch } from "@/redux/store";
import { LoginUser } from "@/api/userAPI";
import { setLoading } from "@/redux/loaderSlice";
import { setUser } from "@/redux/userSlice";
import { toast } from "react-toastify";
import heroImg from "@/assets/login-hero.jpg";
import logo from "@/assets/logo.png";

const LoginUserPage = () => {
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

      if (user.role !== "STUDENT" && user.role !== "TUTOR") {
        toast.error("Please use the admin login page instead.");
        return;
      }

      localStorage.setItem("user", JSON.stringify(user));
      dispatch(setUser(user));
      toast.success("Welcome back!");

      if (user.role === "STUDENT") navigate("/student/dashboard");
      else navigate("/tutor/dashboard");
    } catch (error: any) {
      dispatch(setLoading(false));
      toast.error(error.response?.data?.message || "Login failed");
    }
  };

  return (
    <div className="h-screen flex">
      {/* Left Section - Image/Banner */}
      <div
        className="w-[70%] bg-cover bg-center relative hidden md:block"
        style={{
          backgroundImage: `url(${heroImg})`,
        }}
      >
        <div className="absolute inset-0 bg-blue-500 bg-opacity-30 flex items-center justify-center px-10">
        </div>
      </div>

      {/* Right Section - Form */}
      <div className="w-full md:w-[30%] flex flex-col justify-center items-center bg-white p-10 relative">
        {/* Logo at top */}
        <div className="absolute top-20 mt-10 flex justify-center w-full">
          <img
            src={logo}
            alt="TutorLink Logo"
            className="h-20 object-contain"
          />
        </div>

        <div className="w-full max-w-sm mt-20">
          <h1 className="text-2xl font-bold mb-2 text-primary text-center">
            Login
          </h1>
          <p className="text-sm text-gray-500 mb-5 text-center">
            Welcome back! Please sign in to continue.
          </p>

          <form onSubmit={onSubmit}>
            <input
              className="bg-gray-100 px-3 py-2 rounded-md w-full"
              type="text"
              placeholder="Email"
              {...register("email", { required: true })}
            />
            {errors.email && (
              <p className="mt-1 text-red-500 text-sm">Email is required.</p>
            )}

            <input
              className="mt-3 bg-gray-100 px-3 py-2 rounded-md w-full"
              type="password"
              placeholder="Password"
              {...register("password", { required: true })}
            />
            {errors.password && (
              <p className="mt-1 text-red-500 text-sm">Password is required.</p>
            )}

            <button
              type="submit"
              className="mt-5 w-full bg-primary text-white py-2 rounded-lg hover:bg-opacity-90 transition"
            >
              Sign In
            </button>
          </form>

          <div className="mt-4 text-sm text-center">
            Don’t have an account?{" "}
            <Link to="/register" className="text-primary font-semibold">
              Register now
            </Link>
          </div>

          <div className="mt-2 text-sm text-center">
            Admin?{" "}
            <Link to="/admin/login" className="text-primary font-semibold">
              Go to admin login
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginUserPage;
