import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import { useAppDispatch } from "@/redux/store";
import { RegisterUser } from "@/api/userAPI";
import { setLoading } from "@/redux/loaderSlice";
import { toast } from "react-toastify";
import { useState, useEffect } from "react";
import { getNextStudentId } from "@/api/sequenceAPI";
import heroImg from "@/assets/register-hero.jpg";
import logo from "@/assets/logo.png";

const Register = () => {
  const [selectedRole, setSelectedRole] = useState("");
  const [studentNumber, setStudentNumber] = useState("");

  const {
    register,
    trigger,
    getValues,
    formState: { errors },
  } = useForm();

  const dispatch = useAppDispatch();

  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const isValid = await trigger();
    if (!isValid) return;

    const {
      firstname,
      lastname,
      email,
      password,
      role,
      gradeLevel,
      subject,
    } = getValues();

    try {
      dispatch(setLoading(true));
      const response = await RegisterUser({
        firstname,
        lastname,
        email,
        password,
        role,
        ...(role === "STUDENT" && { studentNumber, gradeLevel }),
        ...(role === "TUTOR" && { subject }),
      });
      dispatch(setLoading(false));

      if (response.status === 200) {
        toast.success(response.data.message);
      } else {
        toast.error(response.data.message);
      }
    } catch (error: any) {
      dispatch(setLoading(false));
      toast.error(error.message);
    }
  };

  useEffect(() => {
    if (selectedRole === "STUDENT") {
      getNextStudentId()
        .then((id) => setStudentNumber(id))
        .catch((err) => console.error(err));
    }
  }, [selectedRole]);

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
      <div className="w-full md:w-[30%] flex flex-col justify-center items-center bg-white p-10 relative overflow-y-auto">
        {/* Logo at top */}
        <div className="absolute top-20 mt-10 flex justify-center w-full">
          <img src={logo} alt="TutorLink Logo" className="h-20 object-contain" />
        </div>

        <div className="w-full max-w-sm mt-24 mb-10">
          <h1 className="text-2xl font-bold mb-2 text-primary text-center">
            Register
          </h1>
          <p className="text-sm text-gray-500 mb-5 text-center">
            Create your account to start your learning journey.
          </p>

          <form onSubmit={onSubmit} method="POST">
            <input
              className="bg-gray-100 px-3 py-2 rounded-md w-full"
              type="text"
              placeholder="First Name"
              {...register("firstname", { required: true, maxLength: 100 })}
            />
            {errors.firstname && (
              <p className="mt-1 text-red-500 text-sm">
                {errors.firstname.type === "required" && "First name is required."}
              </p>
            )}

            <input
              className="mt-3 bg-gray-100 px-3 py-2 rounded-md w-full"
              type="text"
              placeholder="Last Name"
              {...register("lastname", { required: true, maxLength: 100 })}
            />
            {errors.lastname && (
              <p className="mt-1 text-red-500 text-sm">
                {errors.lastname.type === "required" && "Last name is required."}
              </p>
            )}

            <input
              className="mt-3 bg-gray-100 px-3 py-2 rounded-md w-full"
              type="text"
              placeholder="Email"
              {...register("email", {
                required: true,
                pattern: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
              })}
            />
            {errors.email && (
              <p className="mt-1 text-red-500 text-sm">Invalid email address.</p>
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

            <select
              className="mt-3 bg-gray-100 px-3 py-2 rounded-md w-full"
              {...register("role", { required: true })}
              defaultValue=""
              onChange={(e) => setSelectedRole(e.target.value)}
            >
              <option value="" disabled>
                Select role
              </option>
              <option value="STUDENT">Student</option>
              <option value="TUTOR">Tutor</option>
            </select>
            {errors.role && (
              <p className="mt-1 text-red-500 text-sm">Role is required.</p>
            )}

            {/* Student fields */}
            {selectedRole === "STUDENT" && (
              <>
                <input
                  className="mt-3 bg-gray-100 px-3 py-2 rounded-md w-full"
                  type="text"
                  placeholder="Student Number"
                  value={studentNumber}
                  hidden
                />
                <select
                  className="mt-3 bg-gray-100 px-3 py-2 rounded-md w-full"
                  {...register("gradeLevel", { required: true })}
                  defaultValue=""
                >
                  <option value="" disabled>
                    Select Grade Level
                  </option>
                  <option value="Primary School">Primary School</option>
                  <option value="Secondary School">Secondary School</option>
                  <option value="Polytechnic">Polytechnic</option>
                  <option value="JC">JC</option>
                </select>
              </>
            )}

            {/* Tutor fields */}
            {selectedRole === "TUTOR" && (
              <select
                className="mt-3 bg-gray-100 px-3 py-2 rounded-md w-full"
                {...register("subject", { required: true })}
                defaultValue=""
              >
                <option value="" disabled>
                  Select Subject
                </option>
                <option value="English">English</option>
                <option value="Mathematics">Mathematics</option>
                <option value="Biology">Biology</option>
                <option value="Chemistry">Chemistry</option>
                <option value="Physics">Physics</option>
                <option value="Geography">Geography</option>
                <option value="History">History</option>
                <option value="Literature">Literature</option>
              </select>
            )}

            <button
              type="submit"
              className="mt-5 w-full bg-primary text-white py-2 rounded-lg hover:bg-opacity-90 transition"
            >
              Register
            </button>
          </form>

          <div className="mt-4 text-sm text-center">
            Already have an account?{" "}
            <Link to="/" className="text-primary font-semibold">
              Login now
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
