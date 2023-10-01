package main

import (
	sharedApi "CloudServer/src/api"
	"CloudServer/src/config"
	"fmt"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
)

func main() {
	config.LoadEnvironmentAttributes()

	app := fiber.New()
	app.Use(cors.New())

	sharedApi.Append(app)

	var launchErr = app.Listen(":" + fmt.Sprint(config.LISTENING_PORT))
	if launchErr != nil {
		panic(launchErr)
	}
}
