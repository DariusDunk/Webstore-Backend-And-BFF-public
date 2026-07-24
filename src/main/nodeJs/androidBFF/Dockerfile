# Use Node LTS version
FROM node:24

# Set working directory
WORKDIR /app

# Copy package files first to install dependencies
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Expose the port your BFF runs on
EXPOSE 3001

# Default command (can be overridden by docker-compose)
CMD ["npm", "run", "dev"]
