#!/usr/bin/python
# -*- coding: utf-8 -*-

import subprocess
import os

def shellExec(cmd):
	errCode = subprocess.call(cmd, shell=True)
	if errCode != 0:
		fatalError('failed executing: ' + cmd)

def fatalError(message):
	print('[ERROR] ' + message)
	sys.exit()

shellExec('git push origin --all')
shellExec('git push origin --tags')
shellExec('git push gh --all')
shellExec('git push gh --tags')

print('done')
