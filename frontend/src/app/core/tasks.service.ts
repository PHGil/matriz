import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { appSettings } from '../app-settings';
import { Task, TaskChanges, TaskInput, TaskPriority, TaskStatus } from './models';

@Injectable({ providedIn: 'root' })
export class TasksService {
  private readonly http = inject(HttpClient);

  private url(projectId: string): string {
    return `${appSettings.apiBaseUrl}/projects/${projectId}/tasks`;
  }

  /** Optional filters (FR-32); counts in the progress endpoint are never filtered. */
  list(
    projectId: string,
    filters: { status?: TaskStatus | ''; priority?: TaskPriority | '' } = {},
  ): Observable<Task[]> {
    let params = new HttpParams();
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.priority) {
      params = params.set('priority', filters.priority);
    }
    return this.http.get<Task[]>(this.url(projectId), { params });
  }

  get(projectId: string, taskId: string): Observable<Task> {
    return this.http.get<Task>(`${this.url(projectId)}/${taskId}`);
  }

  create(projectId: string, body: TaskInput): Observable<Task> {
    return this.http.post<Task>(this.url(projectId), body);
  }

  /** PATCH: only the fields present are changed; null clears description/dueDate. */
  update(projectId: string, taskId: string, changes: TaskChanges): Observable<Task> {
    return this.http.patch<Task>(`${this.url(projectId)}/${taskId}`, changes);
  }

  delete(projectId: string, taskId: string): Observable<void> {
    return this.http.delete<void>(`${this.url(projectId)}/${taskId}`);
  }
}
