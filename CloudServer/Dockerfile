# Dedicated GoLang image
FROM golang:1.19-alpine

# Establishing internal workdir
WORKDIR /sdu-ra-cloud-server

# Copy go.mod and go.sum files to internal workdir
COPY go.mod go.sum ./

# Updates go.mod and go.sum files based on project usage
RUN go mod tidy
# Verifies dependencies
RUN go mod verify
# Downloads dependencies
RUN go mod download

# Copy the entire project to internal workdir
COPY . .
# Copy the busy wait js file to internal workdir
COPY ../BusyWait/busywait.js ./processes/busy_wait.js
RUN cd processes && npm install

# Compile - builds executable
RUN go build -o cloud-server ./src

RUN sudo apt-get install -y nodejs
RUN echo "nodejs version: $(nodejs --version)"

# Expose port 8889
EXPOSE 8889

# Run the executable
CMD [ "./cloud-server" ]