import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import {
  AutoRefreshTokenService,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  UserActivityService,
  includeBearerTokenInterceptor,
  provideKeycloak,
  withAutoRefreshToken,
} from 'keycloak-angular';
import { appSettings } from './app-settings';
import { routes } from './app.routes';
import { sessionExpiredInterceptor } from './core/session-expired.interceptor';

const apiUrlPattern = new RegExp(`^${appSettings.apiBaseUrl}(/|$)`);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideKeycloak({
      config: { ...appSettings.keycloak },
      initOptions: { onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false },
      features: [
        withAutoRefreshToken({ onInactivityTimeout: 'logout', sessionTimeout: 30 * 60 * 1000 }),
      ],
      providers: [AutoRefreshTokenService, UserActivityService],
    }),
    // The token is only ever sent to our own API (ADR-08).
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [{ urlPattern: apiUrlPattern }],
    },
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor, sessionExpiredInterceptor])),
    provideRouter(routes, withComponentInputBinding()),
  ],
};
