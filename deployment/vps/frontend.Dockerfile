FROM node:22-alpine AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ .
ENV VITE_API_BASE_URL=/fourbubbles-api/api
RUN npm run build -- --base=/fourbubbles/

FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html/fourbubbles
COPY --chmod=644 deployment/vps/nginx.conf /etc/nginx/conf.d/default.conf
USER nginx
EXPOSE 8080
