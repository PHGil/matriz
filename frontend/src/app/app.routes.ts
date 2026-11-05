import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard),
    title: 'Matriz',
  },
  {
    path: 'projetos',
    loadComponent: () => import('./projects/project-list').then((m) => m.ProjectList),
    title: 'Projetos — Matriz',
  },
  {
    path: 'projetos/:id',
    loadComponent: () => import('./projects/project-detail').then((m) => m.ProjectDetail),
    title: 'Projeto — Matriz',
  },
  {
    path: 'projetos/:id/membros',
    loadComponent: () => import('./projects/project-members').then((m) => m.ProjectMembers),
    title: 'Membros — Matriz',
  },
  {
    path: 'projetos/:id/tarefas/:taskId',
    loadComponent: () => import('./tasks/task-detail').then((m) => m.TaskDetail),
    title: 'Tarefa — Matriz',
  },
  { path: '**', redirectTo: '' },
];
