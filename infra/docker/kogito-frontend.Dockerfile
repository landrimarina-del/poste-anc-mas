FROM node:20-alpine AS build
WORKDIR /workspace/apps/kogito/frontend

COPY apps/kogito/frontend/package*.json ./
RUN npm ci

COPY apps/kogito/frontend/ ./
RUN npm run build

FROM alpine:3.20
WORKDIR /opt/frontend
COPY --from=build /workspace/apps/kogito/frontend/dist ./dist

CMD ["sh", "-c", "mkdir -p /frontend-dist && rm -rf /frontend-dist/* && cp -r /opt/frontend/dist/. /frontend-dist/ && chmod -R a+r /frontend-dist && tail -f /dev/null"]

