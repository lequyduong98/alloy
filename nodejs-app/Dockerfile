FROM node:18-slim
WORKDIR /app
COPY package*.json ./
RUN npm install           # QUAN TRỌNG: Dòng này sẽ cài express vào node_modules
COPY . .
CMD ["node", "--require", "@opentelemetry/auto-instrumentations-node/register", "app.js"]