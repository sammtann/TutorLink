import { useEffect, useState } from "react";
import { useNavigate, Outlet } from "react-router-dom";
import { useAppSelector } from "@/redux/store";
import { toast } from "react-toastify";
import Loading from "@/components/Loading";
import { CurrentAdmin } from "@/api/userAPI";

const AdminRoute = () => {
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);

  const { user } = useAppSelector((state) => state.user);
  const navigate = useNavigate();

  // Simulate loading delay (can be removed if not needed)
  useEffect(() => {
    const timer = setTimeout(() => setLoading(false), 1000);
    return () => clearTimeout(timer);
  }, []);

  // Role check
  useEffect(() => {
    if (user?.token) {
      CurrentAdmin(user.token)
        .then(() => setIsAdmin(true))
        .catch(() => setIsAdmin(false));
    } else {
      setIsAdmin(false);
    }
  }, [user]);

  // Redirect if not admin
  useEffect(() => {
    if (!loading && !isAdmin) {
      toast.error("Please login as admin to continue");
      navigate("/admin/login", { replace: true });
    }
  }, [loading, isAdmin, navigate]);

  if (loading) {
    return <Loading />;
  }

  return isAdmin ? <Outlet /> : null;
};

export default AdminRoute;
