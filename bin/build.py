# coding:utf-8

import os
import platform
import shutil


def build_go(os_name, arch):
    print('build hutool on os[%s] for arch[%s]' % (os_name, arch))
    os.system('CGO_ENABLED=0 GOOS=%s GOARCH=%s go build hutool.go' %
              (os_name, arch))
    folder = '../../../target/%s' % os_name
    os.makedirs(folder)
    name = 'hutool'
    if platform.system() == 'Windows':
        name += '.exe'
    shutil.move(name, folder)
    shutil.copy('../../../target/hutool.jar', folder)


os.chdir('..')
print(os.popen('mvn clean package').read())

os.chdir('./src/main/go')

build_go('darwin', 'amd64')
build_go('linux', 'amd64')
build_go('windows', 'amd64')
