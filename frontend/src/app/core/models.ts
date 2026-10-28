export type ProjectRole = 'MANAGER' | 'MEMBER';

export interface StatusCounts {
  todo: number;
  inProgress: number;
  done: number;
  total: number;
}

export interface ProjectSummary {
  id: string;
  name: string;
  description: string | null;
  myRole: ProjectRole;
  counts: StatusCounts;
}

export interface Project {
  id: string;
  name: string;
  description: string | null;
  myRole: ProjectRole;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectInput {
  name: string;
  description: string | null;
}

export interface UserSummary {
  id: string;
  name: string;
  active: boolean;
}

export interface Member {
  user: UserSummary;
  role: ProjectRole;
  addedAt: string;
}

export interface UserDirectoryEntry {
  id: string;
  name: string;
  email: string;
}

export interface Problem {
  title: string;
  status: number;
  detail?: string;
  errors?: { field: string; message: string }[];
}
