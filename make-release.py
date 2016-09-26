#!/usr/bin/python
# -*- coding: utf-8 -*-

import subprocess
import os
import glob
import ConfigParser

def shellExec(cmd):
	errCode = subprocess.call(cmd, shell=True)
	if errCode != 0:
		fatalError('failed executing: ' + cmd)

def fatalError(message):
	print '[ERROR] ' + message
	sys.exit()

def removeIfExists(fileName):
	if os.path.exists(fileName):
		print 'removing ' + fileName + '...'
		os.remove(fileName)

def removeFilesWildcard(pattern):
	files = glob.glob(pattern)
	for file in files:
		os.remove(file)

# zjebany ConfigParser
class FakeSecHead(object):
    def __init__(self, fp):
        self.fp = fp
        self.sechead = '[dummysection]\n'

    def readline(self):
        if self.sechead:
            try: 
                return self.sechead
            finally: 
                self.sechead = None
        else: 
            return self.fp.readline()


apkSrc = '../app/build/outputs/apk/app-debug.apk'
versionFile = '../app/version.properties'
guitarDBDir = 'guitarDB'
releaseDir = 'release'

os.chdir(releaseDir)

# wyciągnięcie numeru wersji
config = ConfigParser.RawConfigParser()
config.readfp(FakeSecHead(open(versionFile)))
versionName = config.get('dummysection', 'VERSION_NAME');

# pliki wyjściowe
apkOutputFile = 'SongBook-' + versionName + '.apk'
outputDBArchive = 'SongBook-db-' + versionName + '.zip'

# usuwanie starszych wersji
removeFilesWildcard('./SongBook-*.apk')
removeFilesWildcard('./SongBook-db-*.zip')

# aktualizacja zmian z bazy źródłowej
print 'updating ' + guitarDBDir + '...'
os.chdir(guitarDBDir)
shellExec('git pull origin master')
shellExec('git merge master')
os.chdir('..')

shellExec('cp ' + apkSrc + ' ' + apkOutputFile)

shellExec('zip -r '+outputDBArchive+' '+guitarDBDir+' -x *.git*')

# nowe pliki do repo gita
shellExec('git add ' + apkOutputFile)
shellExec('git add ' + outputDBArchive)

print 'done'