package api

import (
	"CloudServer/src/dtos"
	"fmt"
	os "os"
	"os/exec"
	"time"
	"uuid"

	main "../main"

	fiber "github.com/gofiber/fiber/v2"
)

// Appends the shared api routes
func Append(app *fiber.App) {
	fmt.Println("Appending shared api routes")
	var version = os.Getenv("API_VERSION")
	if version == "" {
		fmt.Println("API_VERSION environment variable not set, defaulting to v-1")
		version = "v-1"
	}

	//According to Fiber, it uses a go-routing (virtual thread) per request, so this should be safe
	app.Get("/api/"+version+"/metadata", func(c *fiber.Ctx) error {
		c.Context().SetStatusCode(500)
		ApplyStaticLatency()
		return c.SendString("Not Implimented!")
	})

	app.Post("/api/"+version+"/process", func(c *fiber.Ctx) error {
		ApplyStaticLatency()
		//Check if an executable exists:
		var files, err = os.ReadDir("/processes")
		if err != nil {
			c.Response().SetStatusCode(500)
			c.Response().Header.Set(main.DEFAULT_DEBUG_HEADER, "Process directory issues: "+err.Error())
			return c.Next()
		}
		var expectedProcessName = main.PROCESS_NAME
		var found = false
		for _, file := range files {
			if file.Name() == expectedProcessName {
				found = true
				break
			}
		}
		if !found {
			c.Response().SetStatusCode(404)
			c.Response().Header.Set(main.DEFAULT_DEBUG_HEADER, "Process not found: "+expectedProcessName)
			return c.Next()
		}

		uuid := uuid.New()
		const inputFileName = "process_input_" + uuid.String() + ".csv"
		const outputFileName = "process_result_" + uuid.String() + ".txt"

		var writeErr = os.WriteFile(inputFileName, c.Body(), 0644)
		if writeErr != nil {
			c.Response().SetStatusCode(400)
			c.Response().Header.Set(main.DEFAULT_DEBUG_HEADER, "Input file issues: "+writeErr.Error())
			return c.Next()
		}

		var now = time.Now()
		cmd := exec.Command(main.PROCESS_RUN_CMD + " --input-file-name=" + inputFileName + " --output-file-name=" + outputFileName)
		var processRunErr = cmd.Run() //cmd.Run is blocking
		if processRunErr != nil {
			c.Response().SetStatusCode(500)
			c.Response().Header.Set(main.DEFAULT_DEBUG_HEADER, "Process failed to execute: "+processRunErr.Error())
			return c.Next()
		}
		var elapsed = time.Since(now)

		var results, readErr = os.ReadFile(outputFileName)
		if readErr != nil {
			c.Response().SetStatusCode(500)
			c.Response().Header.Set(main.DEFAULT_DEBUG_HEADER, "Output file issues: "+readErr.Error())
			return c.Next()
		}

		var resultStruct = dtos.ProcessResults{
			ComputeMS: elapsed.Milliseconds(),
			Start:     time.Now(),
			Result:    string(results),
		}

		c.Context().SetStatusCode(200)
		return c.JSON(&resultStruct)
	})

}

func ApplyStaticLatency() {
	if main.Latency < 1 {
		return
	}
	time.Sleep(main.Latency * time.Millisecond)
}
