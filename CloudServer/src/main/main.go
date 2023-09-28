package main

import (
	sharedApi "CloudServer/src/api"
	"CloudServer/src/config"
	os "os"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
)

func main() {
	config.LoadEnvironmentAttributes()

	app := fiber.New()
	app.Use(cors.New())

	sharedApi.Append(app)

	var port = os.Getenv("RA_CONTAINER_INTERNAL_PORT")
	if port == "" {
		panic("RA_CONTAINER_INTERNAL_PORT environment variable not set")
	}

	var launchErr = app.Listen(":" + port)
	if launchErr != nil {
		panic(launchErr)
	}
}
