import { useEffect, useState } from "react";
import { useNavigate, Outlet } from "react-router-dom";
import { useAppSelector } from "@/redux/store";
import { toast } from "react-toastify";
import Loading from "@/components/Loading";
import { CurrentUser } from "@/api/userAPI";

const UserRoute = () => {
  const [loading, setLoading] = useState(true);
  const [isUser, setIsUser] = useState(false);

  const { user } = useAppSelector((state) => state.user);
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setTimeout(() => setLoading(false), 1000);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (user?.token) {
      CurrentUser(user.token)
        .then(() => setIsUser(true))
        .catch(() => setIsUser(false));
    } else {
      setIsUser(false);
    }
  }, [user]);

  useEffect(() => {
    if (!loading && !isUser) {
      toast.error("Please login as user to continue");
      navigate("/", { replace: true });
    }
  }, [loading, isUser, navigate]);

  if (loading) {
    return <Loading />;
  }

  return isUser ? <Outlet /> : null;
};

export default UserRoute;
