import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { appSettings } from '../app-settings';
import { Member, Project, ProjectInput, ProjectSummary, UserDirectoryEntry } from './models';

@Injectable({ providedIn: 'root' })
export class ProjectsService {
  private readonly http = inject(HttpClient);
  private readonly base = `${appSettings.apiBaseUrl}/projects`;

  list(q?: string): Observable<ProjectSummary[]> {
    const params = q?.trim() ? new HttpParams().set('q', q.trim()) : undefined;
    return this.http.get<ProjectSummary[]>(this.base, { params });
  }

  get(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.base}/${id}`);
  }

  create(body: ProjectInput): Observable<Project> {
    return this.http.post<Project>(this.base, body);
  }

  update(id: string, body: ProjectInput): Observable<Project> {
    return this.http.patch<Project>(`${this.base}/${id}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  members(id: string): Observable<Member[]> {
    return this.http.get<Member[]>(`${this.base}/${id}/members`);
  }

  addMember(id: string, userId: string): Observable<Member> {
    return this.http.post<Member>(`${this.base}/${id}/members`, { userId });
  }

  removeMember(id: string, userId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}/members/${userId}`);
  }

  searchUsers(q: string): Observable<UserDirectoryEntry[]> {
    return this.http.get<UserDirectoryEntry[]>(`${appSettings.apiBaseUrl}/users`, {
      params: new HttpParams().set('q', q.trim()),
    });
  }
}
