package api

import (
	dtos "CloudServer/src/dtos"
	"fmt"
	os "os"
	"os/exec"
	"strings"
	"time"

	systemDiagnostics "CloudServer/src/services"

	uuid "github.com/google/uuid"

	config "CloudServer/src/config"

	fiber "github.com/gofiber/fiber/v2"
)

var actions map[string]dtos.ServerAction

// Appends the shared api routes
func Append(app *fiber.App) {
	fmt.Println("Appending shared api routes")
	var version = config.API_VERSION

	actions = generateServerActions()

	//According to Fiber, it uses a go-routing (virtual thread) per request, so this should be safe
	app.Get("/api/"+version+"/metadata", func(c *fiber.Ctx) error {
		ApplyStaticLatency()

		c.Context().SetStatusCode(200)
		var cpu, cpuReadErr = systemDiagnostics.GetSystemCPUUsage()
		var mem, memReadErr = systemDiagnostics.GetSystemRAMUsage()
		if cpuReadErr != nil {
			cpu = -1
			c.Response().Header.Set(config.DEFAULT_DEBUG_HEADER, "CPU read error: "+cpuReadErr.Error())
			c.Context().SetStatusCode(206)
		}
		if memReadErr != nil {
			mem = -1
			c.Response().Header.Set(config.DEFAULT_DEBUG_HEADER, "Memory read error: "+memReadErr.Error())
			c.Context().SetStatusCode(206)
		}

		var resultStruct = dtos.ServerMetadata{
			Id:            config.ENVIRONMENT_ID,
			Actions:       actions,
			Specification: config.SPECIFICATION,
			Utilization: dtos.SystemUtilization{
				Cpu:  cpu,
				Mem:  mem,
				Disk: -1,
			},
		}

		return c.JSON(&resultStruct)
	})

	app.Post("/api/"+version+"/process", startProcess)
}

func generateServerActions() map[string]dtos.ServerAction {
	return map[string]dtos.ServerAction{
		"getMetadata": {
			Method: "GET",
			Uri:    "/api/" + config.API_VERSION + "/metadata",
			Ip:     config.CONTAINER_HOST_IP,
			Port:   config.CONTAINER_EXTERNAL_PORT,
		},
		"startProcess": {
			Method: "POST",
			Uri:    "/api/" + config.API_VERSION + "/process",
			Ip:     config.CONTAINER_HOST_IP,
			Port:   config.CONTAINER_EXTERNAL_PORT,
		},
		"shutdown": {
			Method: "POST",
			Uri:    "/api/" + config.API_VERSION + "/shutdown",
			Ip:     config.CONTAINER_HOST_IP,
			Port:   config.CONTAINER_EXTERNAL_PORT,
		},
	}
}

func startProcess(c *fiber.Ctx) error {
	ApplyStaticLatency()
	//Check if an executable exists:
	var files, err = os.ReadDir("/processes")
	if err != nil {
		c.Response().SetStatusCode(500)
		c.Response().Header.Set(config.DEFAULT_DEBUG_HEADER, "Process directory issues: "+err.Error())
		return c.Next()
	}
	var expectedProcessName = config.PROCESS_NAME
	var found = false
	for _, file := range files {
		if file.Name() == expectedProcessName {
			found = true
			break
		}
	}
	if !found { //if the expected executable doesn't exist, return 404
		c.Response().SetStatusCode(404)
		c.Response().Header.Set(config.DEFAULT_DEBUG_HEADER, "Process not found: "+expectedProcessName)
		return c.Next()
	}

	uuid := uuid.New() //calc input and output file names for future reference
	var inputFileName = "process_input_" + uuid.String() + ".csv"
	var outputFileName = "process_result_" + uuid.String() + ".txt"

	//remove old i/o files if they exist
	var wdFiles, readWDErr = os.ReadDir("./")
	if readWDErr == nil {
		cleanWDOfOldFiles(wdFiles)
	} else {
		fmt.Println("Failed to read WD, unable to clean it: " + readWDErr.Error())
	}

	var writeErr = os.WriteFile(inputFileName, c.Body(), 0644)
	if writeErr != nil { //take csv content of the request body and save it as a file at WD root.
		c.Response().SetStatusCode(400)
		c.Response().Header.Set(config.DEFAULT_DEBUG_HEADER, "Input file issues: "+writeErr.Error())
		return c.Next()
	}

	var now = time.Now()
	//Then run the process with the input- and the output file as an argument
	cmd := exec.Command(config.PROCESS_RUN_CMD + " --input-file-name=" + inputFileName + " --output-file-name=" + outputFileName)
	var processRunErr = cmd.Run() //cmd.Run is blocking
	if processRunErr != nil {     //this is
		c.Response().SetStatusCode(500)
		c.Response().Header.Set(config.DEFAULT_DEBUG_HEADER, "Process failed to execute: "+processRunErr.Error())
		return c.Next()
	}
	var elapsed = time.Since(now)

	var results, readErr = os.ReadFile(outputFileName)
	if readErr != nil {
		c.Response().SetStatusCode(500)
		c.Response().Header.Set(config.DEFAULT_DEBUG_HEADER, "Output file issues: "+readErr.Error())
		return c.Next()
	}

	var resultStruct = dtos.ProcessResults{
		ComputeMS: elapsed.Milliseconds(),
		Start:     time.Now(),
		Result:    string(results),
	}

	c.Context().SetStatusCode(200)
	return c.JSON(&resultStruct)
}

func cleanWDOfOldFiles(files []os.DirEntry) {
	for _, file := range files {
		var currentName = file.Name()
		if strings.Contains(currentName, "process_input_") || strings.Contains(currentName, "process_result_") {
			os.Remove(currentName)
		}
	}
}

func ApplyStaticLatency() {
	if config.LATENCY < 1 {
		return
	}
	time.Sleep(time.Duration(config.LATENCY) * time.Millisecond)
}
