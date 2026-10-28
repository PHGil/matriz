import { HttpClient } from '@angular/common/http';
import { Injectable, effect, inject, signal } from '@angular/core';
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEventType } from 'keycloak-angular';
import Keycloak from 'keycloak-js';
import { appSettings } from '../app-settings';

export interface CurrentUser {
  id: string;
  name: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly keycloak = inject(Keycloak);
  private readonly keycloakEvent = inject(KEYCLOAK_EVENT_SIGNAL);

  readonly user = signal<CurrentUser | null>(null);
  readonly sessionExpired = signal(false);

  constructor() {
    effect(() => {
      const event = this.keycloakEvent();
      if (
        event.type === KeycloakEventType.AuthRefreshError ||
        event.type === KeycloakEventType.AuthLogout
      ) {
        this.sessionExpired.set(true);
      }
    });
  }

  loadCurrentUser(): void {
    this.http.get<CurrentUser>(`${appSettings.apiBaseUrl}/auth/me`).subscribe({
      next: (user) => this.user.set(user),
      error: () => this.user.set(null),
    });
  }

  markSessionExpired(): void {
    this.sessionExpired.set(true);
  }

  login(): void {
    void this.keycloak.login({ redirectUri: window.location.href });
  }

  logout(): void {
    void this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
