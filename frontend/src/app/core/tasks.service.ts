import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { appSettings } from '../app-settings';
import { Task, TaskInput } from './models';

@Injectable({ providedIn: 'root' })
export class TasksService {
  private readonly http = inject(HttpClient);

  private url(projectId: string): string {
    return `${appSettings.apiBaseUrl}/projects/${projectId}/tasks`;
  }

  list(projectId: string): Observable<Task[]> {
    return this.http.get<Task[]>(this.url(projectId));
  }

  get(projectId: string, taskId: string): Observable<Task> {
    return this.http.get<Task>(`${this.url(projectId)}/${taskId}`);
  }

  create(projectId: string, body: TaskInput): Observable<Task> {
    return this.http.post<Task>(this.url(projectId), body);
  }
}
