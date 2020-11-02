# coding:utf-8

import os
import shutil
import re

os.chdir('..')
print(os.popen('git pull').read())
print(os.popen('mvn clean package').read())

with open('./pom.xml', 'r', encoding='utf-8') as fr:
    res = re.search('<version>(.*?)</version>',
                    fr.read(), re.M | re.I)
    version = res.group(1)


def build_go(os_name, arch):
    print('build hutool on os[%s] for arch[%s]' % (os_name, arch))
    os.system('CGO_ENABLED=0 GOOS=%s GOARCH=%s go build hutool.go' %
              (os_name, arch))
    os.chdir('../../../target')
    os.makedirs(os_name+'/bin')
    name = '../src/main/go/hutool'
    if os_name == 'windows':
        name += '.exe'
    shutil.move(name, os_name+'/bin')
    shutil.copy('hutool.jar', os_name)
    print(os.popen('zip -r %s-%s.zip %s' %
                   (os_name, version, os_name)).read())
    os.chdir('../src/main/go')


os.chdir('./src/main/go')

build_go('darwin', 'amd64')
build_go('linux', 'amd64')
build_go('windows', 'amd64')
