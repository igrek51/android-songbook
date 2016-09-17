#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
from subprocess import call, check_output

def shellExec(cmd):
	errCode = call(cmd, shell=True)
	if errCode != 0:
		fatalError('failed executing: ' + cmd)

def fatalError(message):
	print '[ERROR] ' + message
	sys.exit()

shellExec('git checkout pl-lang')
shellExec('git merge master')
shellExec('git checkout master')

print 'done'