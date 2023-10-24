package config

import (
	dtos "CloudServer/src/dtos"
	"fmt"
	"os"
	"strconv"
)

var IRN string = "unknown"
var ENVIRONMENT_ID int64 = -1
var CPUS float32 = -1.1
var MEMORY int32 = -1
var LATENCY int32 = -1
var STORAGE int32 = -1
var PROCESS_RUN_CMD string = "none"
var PROCESS_NAME string = "none"
var DEFAULT_DEBUG_HEADER string = "SDU_RA_Debug_Header"
var CONTAINER_EXTERNAL_PORT int32 = -1
var LISTENING_PORT int32 = 4242
var CONTAINER_HOST_IP string = "unknown"
var API_VERSION string = "v-1"

// Invalid until environment has been loaded
var SPECIFICATION dtos.ServerSpecification

// Attempts to load the environment variables:
// RA_IRN,
// RA_ENVIRONMENT_ID,
// RA_CPUS,
// RA_MEMORY,
// RA_STATIC_LATENCY,
// RA_STORAGE,
// RA_CONTAINER_EXTERNAL_PORT,
// RA_CONTAINER_HOST_IP,
// API_VERSION,
// RA_INTERNAL_SERVER_PORT,
// RA_PROCESS_RUN_CMD_ROOT &
// RA_PROCESS_NAME
func LoadEnvironmentAttributes() {
	var irn = os.Getenv("RA_IRN")
	if irn == "" {
		fmt.Println("RA_IRN environment variable not set, defaulting to unknown")
	} else {
		IRN = irn
	}
	var id = os.Getenv("RA_ENVIRONMENT_ID")
	var idAsInt, parseIntErr = strconv.ParseInt(id, 10, 64)
	if parseIntErr != nil {
		fmt.Println("RA_ENVIRONMENT_ID environment variable not valid: + " + id + ", defaulting to -1")
	} else {
		ENVIRONMENT_ID = idAsInt
	}
	var cpus = os.Getenv("RA_CPUS")
	var cpusAsFloat, parseFloatErr = strconv.ParseFloat(cpus, 32)
	if parseFloatErr != nil {
		fmt.Println("RA_CPUS environment variable not valid: + " + cpus + ", defaulting to -1.1")
	} else {
		CPUS = float32(cpusAsFloat)
	}
	var memory = os.Getenv("RA_MEMORY")
	var memoryAsInt, parseIntErr2 = strconv.ParseInt(memory, 10, 32)
	if parseIntErr2 != nil {
		fmt.Println("RA_MEMORY environment variable not valid: + " + memory + ", defaulting to -1")
	} else {
		MEMORY = int32(memoryAsInt)
	}
	var latency = os.Getenv("RA_STATIC_LATENCY")
	var latencyAsInt, parseIntErrLatency = strconv.ParseInt(latency, 10, 32)
	if parseIntErrLatency != nil {
		fmt.Println("RA_LATENCY environment variable not valid: + " + latency + ", defaulting to 0")
	} else {
		LATENCY = int32(latencyAsInt)
	}
	var storage = os.Getenv("RA_STORAGE")
	var storageAsInt, parseIntErrStorage = strconv.ParseInt(storage, 10, 32)
	if parseIntErrStorage != nil {
		fmt.Println("RA_STORAGE environment variable not valid: + " + storage + ", defaulting to -1")
	} else {
		STORAGE = int32(storageAsInt)
	}
	var processRunCmd = os.Getenv("RA_PROCESS_RUN_CMD_ROOT")
	if processRunCmd == "" {
		fmt.Println("RA_PROCESS_RUN_CMD environment variable not set, defaulting to none")
	} else {
		PROCESS_RUN_CMD = processRunCmd
	}
	var processName = os.Getenv("RA_PROCESS_NAME")
	if processName == "" {
		fmt.Println("RA_PROCESS_NAME environment variable not set, defaulting to none")
	} else {
		PROCESS_NAME = processName
	}
	var containerExternalPort = os.Getenv("RA_CONTAINER_EXTERNAL_PORT")
	var containerExternalPortAsInt, parseIntErrContainerExternalPort = strconv.ParseInt(containerExternalPort, 10, 32)
	if parseIntErrContainerExternalPort != nil || containerExternalPortAsInt < 0 || containerExternalPortAsInt > 65535 {
		fmt.Println("RA_CONTAINER_EXTERNAL_PORT environment variable not valid: " + containerExternalPort + ", defaulting to -1")
	} else {
		CONTAINER_EXTERNAL_PORT = int32(containerExternalPortAsInt)
	}
	var containerHostIp = os.Getenv("RA_CONTAINER_HOST_IP")
	if containerHostIp == "" {
		fmt.Println("RA_CONTAINER_HOST_IP environment variable not set, defaulting to unknown")
	} else {
		CONTAINER_HOST_IP = containerHostIp
	}
	var apiVersion = os.Getenv("RA_API_VERSION")
	if apiVersion == "" {
		fmt.Println("RA_API_VERSION environment variable not set, defaulting to v-1")
	} else {
		API_VERSION = apiVersion
	}
	var listeningPort = os.Getenv("RA_INTERNAL_SERVER_PORT")
	if listeningPort == "" {
		fmt.Println("RA_INTERNAL_SERVER_PORT environment variable not set, defaulting to 8080")
	} else {
		var listeningPortAsInt, parseIntErrListeningPort = strconv.ParseInt(listeningPort, 10, 32)
		if parseIntErrListeningPort != nil {
			fmt.Println("RA_INTERNAL_SERVER_PORT environment variable not valid: + " + listeningPort + ", defaulting to 8080")
		} else {
			LISTENING_PORT = int32(listeningPortAsInt)
		}
	}

	SPECIFICATION = dtos.ServerSpecification{
		Irn:     IRN,
		Memory:  MEMORY,
		Cpus:    CPUS,
		Storage: STORAGE,
		Latency: LATENCY,
	}
}
