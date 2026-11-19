FROM node:26-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build

# Serves the built app; /api is proxied to BACKEND_URL.
FROM nginx:1.29-alpine
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /src/dist/frontend/browser /usr/share/nginx/html
COPY --chmod=755 40-matriz-config.sh /docker-entrypoint.d/40-matriz-config.sh
ENV BACKEND_URL=http://backend:8080
ENV KEYCLOAK_URL=http://localhost:8180
EXPOSE 80
