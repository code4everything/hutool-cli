package main

import (
	"bytes"
	"fmt"
	"log"
	"os"
	"os/exec"
)

func main() {
	args := append([]string{"-jar", "hutool.jar"}, os.Args[1:]...)
	cmd := exec.Command("java", args...)
	path := os.Getenv("HUTOOL_PATH")
	if path == "" {
		log.Fatalf("evirement 'HUTOOL_PATH' not found!")
		return
	}
	cmd.Dir = path

	var out bytes.Buffer
	var stderr bytes.Buffer
	cmd.Stdout = &out
	cmd.Stderr = &stderr
	err := cmd.Run()
	if err == nil {
		fmt.Println(out.String())
	} else {
		log.Fatalf(err.Error(), stderr.String())
	}
}
