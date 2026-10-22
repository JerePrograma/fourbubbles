FROM node:22-alpine AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ .
ARG PUBLIC_DEMO_ENABLED=false
ARG PUBLIC_DEMO_USERNAME
ARG PUBLIC_DEMO_PASSWORD
ENV VITE_API_BASE_URL=/fourbubbles-api/api
ENV VITE_PUBLIC_DEMO_ENABLED=$PUBLIC_DEMO_ENABLED
ENV VITE_PUBLIC_DEMO_USERNAME=$PUBLIC_DEMO_USERNAME
ENV VITE_PUBLIC_DEMO_PASSWORD=$PUBLIC_DEMO_PASSWORD
RUN node -e "const e=process.env;if(!['true','false'].includes(e.VITE_PUBLIC_DEMO_ENABLED))throw Error('Invalid public demo flag');if(e.VITE_PUBLIC_DEMO_ENABLED==='true'&&(!/^[A-Za-z0-9_.@-]{3,100}$/.test(e.VITE_PUBLIC_DEMO_USERNAME||'')||/admin/i.test(e.VITE_PUBLIC_DEMO_USERNAME||'')||(e.VITE_PUBLIC_DEMO_PASSWORD||'').length<24||(e.VITE_PUBLIC_DEMO_PASSWORD||'').length>72))throw Error('Invalid public viewer configuration');"
RUN npm run build -- --base=/fourbubbles/

FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html/fourbubbles
COPY --chmod=644 deployment/vps/nginx.conf /etc/nginx/conf.d/default.conf
USER nginx
EXPOSE 8080
