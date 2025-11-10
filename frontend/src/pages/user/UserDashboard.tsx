import { useNavigate } from "react-router-dom";
import { useAppDispatch } from "@/redux/store";
import { setUser } from "@/redux/userSlice";

const UserDashboard = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const handleLogout = () => {
    //clear local storage
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    //clear redux
    dispatch(setUser(null));
    //redirect
    navigate("/");
  };
  return (
    <div className="h-screen bg-primary flex items-center justify-center p-5 overflow-hidden">
      {/* Container */}
      <div className="flex flex-col items-center ">
        <div className="bg-white h-full w-[400px] rounded-md p-5">
          {/* Header */}
          <div className="p-5 text-center">
            <h1 className="font-bold text-xl">Welcome, User Dashboard</h1>
          </div>
          <button
            onClick={handleLogout}
            className="mt-3 rounded-lg bg-primary text-white w-full px-20 py-2 transition duration-500 hover:bg-gray-200 hover:text-primary ">
            Logout
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;
