# Dedicated GoLang image
FROM golang:1.19-alpine

# Establishing internal workdir
WORKDIR /sdu-ra-cloud-server
#How to run the process - with no flags or args
#Right now windows powershell for reference but should be translated to ubuntu bash
#Notice the "--" surpurfluos seperator as part of the npm run start, it tells npm to account for the following arguments following it.
ENV RA_PROCESS_RUN_CMD_ROOT="cd ./processes/BusyWait && npm run start --"
ENV RA_PROCESS_NAME="BusyWait"
ENV RA_MEMORY=-1
ENV RA_IRN="unknown"
ENV RA_ENVIRONMENT_ID=-1
ENV RA_CPUS=-1
ENV RA_STATIC_LATENCY=-1
ENV RA_STORAGE=-1
ENV RA_INTERNAL_SERVER_PORT=8889
ENV RA_CONTAINER_EXTERNAL_PORT=8889
ENV RA_CONTAINER_HOST_IP="0.0.0.0"
ENV RA_API_VERSION="v1"

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
COPY ../BusyWait/* ./processes/BusyWait
RUN cd processes && npm install

# Compile - builds executable
RUN go build -o cloud-server ./src

RUN sudo apt-get install -y nodejs
RUN echo "nodejs version: $(nodejs --version)"


# Expose port 8889
EXPOSE 8889

# Run the executable
CMD [ "./cloud-server" ]