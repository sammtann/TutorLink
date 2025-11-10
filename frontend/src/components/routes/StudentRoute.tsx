import { useEffect, useState } from "react";
import { useNavigate, Outlet } from "react-router-dom";
import { useAppSelector } from "@/redux/store";
import { toast } from "react-toastify";
import Loading from "@/components/Loading";
import { CurrentStudent } from "@/api/userAPI";

const StudentRoute = () => {
  const [loading, setLoading] = useState(true);
  const [isStudent, setIsStudent] = useState(false);

  const { user } = useAppSelector((state) => state.user);
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setTimeout(() => setLoading(false), 1000);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (user?.token) {
      CurrentStudent(user.token)
        .then(() => setIsStudent(true))
        .catch(() => setIsStudent(false));
    } else {
      setIsStudent(false);
    }
  }, [user]);

  useEffect(() => {
    if (!loading && !isStudent) {
      toast.error("Please login as student to continue");
      navigate("/", { replace: true });
    }
  }, [loading, isStudent, navigate]);

  if (loading) {
    return <Loading />;
  }

  return isStudent ? <Outlet /> : null;
};

export default StudentRoute;
