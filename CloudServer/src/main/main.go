package main

import (
	sharedApi "CloudServer/src/api"
	"CloudServer/src/config"
	"fmt"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
)

func main() {
	config.LoadEnvironmentAttributes()

	app := fiber.New()
	app.Use(cors.New())
	app.Use(logRequests)

	sharedApi.Append(app)

	var launchErr = app.Listen(":" + fmt.Sprint(config.LISTENING_PORT))
	if launchErr != nil {
		panic(launchErr)
	}
}

func logRequests(c *fiber.Ctx) error {
	fmt.Println("Request recieved: ", c.Method(), c.Path(), "\t\t at ", time.Now().Format(time.RFC3339), " from ", c.IP())
	return c.Next()
}
