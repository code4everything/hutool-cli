package main

import (
	"bufio"
	"bytes"
	"fmt"
	"golang.org/x/text/encoding/simplifiedchinese"
	"io"
	"log"
	"os"
	"os/exec"
	"runtime"
)

type Charset string

const (
	UTF8    = Charset("UTF-8")
	GB18030 = Charset("GB18030")
)

func main() {
	cwd, _ := os.Getwd()
	args := append([]string{"-jar", "hutool.jar", "--work-dir", cwd}, os.Args[1:]...)
	cmd := exec.Command("java", args...)
	path := os.Getenv("HUTOOL_PATH")
	if path == "" {
		log.Fatalf("environment 'HUTOOL_PATH' not found!")
		return
	}
	cmd.Dir = path

	// var stdoutBuf, stderrBuf bytes.Buffer
	var stderrBuf bytes.Buffer
	stdoutIn, _ := cmd.StdoutPipe()
	stderrIn, _ := cmd.StderrPipe()
	var errStdout, errStderr error
	// stdout := io.MultiWriter(os.Stdout, &stdoutBuf)
	stderr := io.MultiWriter(os.Stderr, &stderrBuf)
	err := cmd.Start()
	if err != nil {
		log.Fatalf("cmd.Start() failed with '%s'\n", err)
	}
	go func() {
		// _, errStdout = io.Copy(stdout, stdoutIn)
		in := bufio.NewScanner(stdoutIn)
		for in.Scan() {
			charset := UTF8
			if runtime.GOOS == "windows" {
				charset = GB18030
			}
			cmdRe := ConvertByte2String(in.Bytes(), charset)
			fmt.Println(cmdRe)
		}
	}()
	go func() {
		_, errStderr = io.Copy(stderr, stderrIn)
	}()
	err = cmd.Wait()
	if err != nil {
		log.Fatalf("cmd.Run() failed with %s\n", err)
	}
	if errStdout != nil || errStderr != nil {
		log.Fatal("failed to capture stdout or stderr\n")
	}
	fmt.Println()
}

func ConvertByte2String(byte []byte, charset Charset) string {
	var str string
	switch charset {
	case GB18030:
		var decodeBytes, _ = simplifiedchinese.GB18030.NewDecoder().Bytes(byte)
		str = string(decodeBytes)
	case UTF8:
		fallthrough
	default:
		str = string(byte)
	}
	return str
}
