# coding:utf-8

import os
import platform


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
bin_folder = './hutool/bin'
if not os.path.exists(bin_folder):
    os.makedirs(bin_folder)
if platform.system() == 'Windows':
    hutool_file = 'hutool.exe'
else:
    hutool_file = 'hutool'
remove(bin_folder + '/' + hutool_file)
os.rename('./src/main/go/' + hutool_file, bin_folder + '/' + hutool_file)
