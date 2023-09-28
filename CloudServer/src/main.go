package main

import (
	sharedApi "CloudServer/src/api"
	"fmt"
	os "os"
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
)

var IRN = "unknown"
var ENVIRONMENT_ID = -1
var CPUS = -1.1
var MEMORY = -1
var LATENCY = -1
var STORAGE = -1

func main() {
	fmt.Println("This is Cloud Server program, when I've build it that is.")

	app := fiber.New()
	app.Use(cors.New())

	sharedApi.Append(app)

	var port = os.Getenv("CONTAINER_INTERNAL_PORT")
	if port == "" {
		panic("CONTAINER_INTERNAL_PORT environment variable not set")
	}

	loadEnvironmentAttributes()

	var launchErr = app.Listen(":" + port)
	if launchErr != nil {
		panic(launchErr)
	}
}

// Attempts to load the environment variables:
// IRN,
// ENVIRONMENT_ID,
// CPUS,
// MEMORY,
// LATENCY &
// STORAGE
func loadEnvironmentAttributes() {
	var irn = os.Getenv("IRN")
	if irn == "" {
		fmt.Println("IRN environment variable not set, defaulting to unknown")
	} else {
		IRN = irn
	}
	var id = os.Getenv("ENVIRONMENT_ID")
	var idAsInt, parseIntErr = strconv.Atoi(id)
	if parseIntErr != nil {
		fmt.Println("ENVIRONMENT_ID environment variable not valid: + " + id + ", defaulting to -1")
	} else {
		ENVIRONMENT_ID = idAsInt
	}
	var cpus = os.Getenv("CPUS")
	var cpusAsFloat, parseFloatErr = strconv.ParseFloat(cpus, 32)
	if parseFloatErr != nil {
		fmt.Println("CPUS environment variable not valid: + " + cpus + ", defaulting to -1.1")
	} else {
		CPUS = cpusAsFloat
	}
	var memory = os.Getenv("MEMORY")
	var memoryAsInt, parseIntErr2 = strconv.Atoi(memory)
	if parseIntErr2 != nil {
		fmt.Println("MEMORY environment variable not valid: + " + memory + ", defaulting to -1")
	} else {
		MEMORY = memoryAsInt
	}
	var latency = os.Getenv("LATENCY")
	var latencyAsInt, parseIntErrLatency = strconv.Atoi(latency)
	if parseIntErrLatency != nil {
		fmt.Println("LATENCY environment variable not valid: + " + latency + ", defaulting to 0")
	} else {
		LATENCY = latencyAsInt
	}
	var storage = os.Getenv("STORAGE")
	var storageAsInt, parseIntErrStorage = strconv.Atoi(storage)
	if parseIntErrStorage != nil {
		fmt.Println("STORAGE environment variable not valid: + " + storage + ", defaulting to -1")
	} else {
		STORAGE = storageAsInt
	}
}
