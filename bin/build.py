# coding:utf-8

import os
import platform


def build_go(os_name, arch):
    os.system("SET GOOS=%s" % os_name)
    os.system('SET GOARCH=%s' % arch)
    print('build hutool on %s' % os_name)
    os.system('go build hutool.go')


os.chdir('..')
# print(os.popen('mvn clean package').read())
os.system('SET CGO_ENABLED=0')
os.chdir('./src/main/go')
build_go('darwin', 'amd64')
build_go('linux', 'amd64')
build_go('windows', 'amd64')
