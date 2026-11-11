# Serves the already built app (run `npx ng build` first); /api is proxied to BACKEND_URL.
FROM nginx:1.29-alpine
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY dist/frontend/browser /usr/share/nginx/html
ENV BACKEND_URL=http://backend:8080
EXPOSE 80
