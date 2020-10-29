# coding:utf-8

import os
import re
import shutil

def remove(filename):
    if os.path.exists(filename):
        os.remove(filename)

os.chdir('..')
print(os.popen('git pull').read())
print(os.popen('mvn clean package').read())

filename = './hutool/hutool.jar'
remove(filename)
os.rename('./target/hutool.jar', filename)

os.chdir('./src/main/go')
print(os.popen('go build hutool.go').read())

os.chdir('../../..')
filename = './hutool/bin/hutool.exe'
remove(filename)
os.rename('./src/main/go/hutool.exe', filename)
