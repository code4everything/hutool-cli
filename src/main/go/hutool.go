package main

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
)

func main() {

	java := strings.Join([]string{os.Getenv("JAVA_HOME"), "bin", "java"}, string(os.PathSeparator))
	fmt.Println(java)
	cmdTest := exec.Command(java)
	out, err0 := cmdTest.CombinedOutput()
	if err0 != nil {
		log.Fatalf("execute java failed with %s\n", err0)
		return
	}
	fmt.Println(string(out))

	args := append([]string{"-jar", "hutool.jar"}, os.Args[1:]...)
	fmt.Println(args)
	cmd := exec.Command("java", args...)
	path := os.Getenv("HUTOOL_PATH")
	if path == "" {
		log.Fatalf("evirement 'HUTOOL_PATH' not found!")
		return
	}
	fmt.Println(path)
	cmd.Dir = path
	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Fatalf("execute failed with %s\n", err)
		return
	}
	fmt.Println(string(out))
}
