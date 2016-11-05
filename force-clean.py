#!/usr/bin/python
# -*- coding: utf-8 -*-

# wykonać w przypadku błędu cleana przez Android Studio, które samo blokuje sobie pliki do usunięcia, KURWA!!!

import sys
from subprocess import call, check_output

def shellExec(cmd):
	errCode = call(cmd, shell=True)
	if errCode != 0:
		fatalError('failed executing: ' + cmd)

def fatalError(message):
	print('[ERROR] ' + message)
	sys.exit()

shellExec('rm -rf app/build')

print('done')
