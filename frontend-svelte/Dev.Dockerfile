FROM node:16-slim
WORKDIR /app
RUN apt-get update
RUN apt-get install -y python3-pip
COPY package.json .
COPY package-lock.json .
RUN npm install
COPY svelte.config.js .
COPY vite.config.js .
COPY jsconfig.json .
COPY playwright.config.js .
# Note that src is mounted so changes will occur.
ENTRYPOINT npm run dev