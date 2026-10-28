import { Routes } from '@angular/router';
import { Dashboard } from './dashboard/dashboard';
import { ProjectDetail } from './projects/project-detail';
import { ProjectList } from './projects/project-list';
import { ProjectMembers } from './projects/project-members';

export const routes: Routes = [
  { path: '', component: Dashboard, title: 'Matriz' },
  { path: 'projetos', component: ProjectList, title: 'Projetos — Matriz' },
  { path: 'projetos/:id', component: ProjectDetail, title: 'Projeto — Matriz' },
  { path: 'projetos/:id/membros', component: ProjectMembers, title: 'Membros — Matriz' },
  { path: '**', redirectTo: '' },
];
