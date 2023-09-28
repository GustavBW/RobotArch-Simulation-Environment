package api

import (
	"fmt"
	os "os"

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
		return c.SendString("Not Implimented!")
	})

	app.Post("/api/"+version+"/process", func(c *fiber.Ctx) error {
		c.Context().SetStatusCode(500)
		return c.SendString("Not Implimented!")
	})

}
