import { Tutor } from "./TutorType";

export interface TransactionSummary {
  description: string;
  amount: number;
}

export interface MonthlyEarnings {
  month: string;
  total: number;
}

export interface TransactionMetrics {
  totalEarnings: number;
  commissionCollected: number;
  highestTransaction: TransactionSummary | null;
  monthlyEarnings: MonthlyEarnings[];
}

export interface AdminDashboardType {
  totalUsers: number;
  activeUsers: number;
  suspendedUsers: number;

  totalTutors: number;
  activeTutors: number;
  suspendedTutors: number;
  unverifiedTutors: number;

  totalStudents: number;
  activeStudents: number;
  suspendedStudents: number;

  totalAdmins: number;
  activeAdmins: number;
  suspendedAdmins: number;
  pendingTutors: Tutor[];

  transactionMetrics?: TransactionMetrics; // optional if backend may not return it yet
}