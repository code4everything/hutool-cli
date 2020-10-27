package main

import (
	"fmt"
	"log"
	"os"
	"os/exec"
)

func main() {
	args := append([]string{"-jar", "hutool.jar"}, os.Args...)
	cmd := exec.Command("java", args...)
	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Fatalf("execute failed with %s\n", err)
	}
	fmt.Printf(string(out))
}
