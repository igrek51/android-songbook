#!/usr/bin/python
# -*- coding: utf-8 -*-

import subprocess
import os

apkSrc = '../app/build/outputs/apk/app-debug.apk'
# TODO wyciągnąć numer wersji z app/version.properties
versionName = '1.0.2'

apkOutputFile = 'SongBook-' + versionName + '-debug.apk'
outputDBArchive = 'SongBook-db-' + versionName + '.zip'

def removeIfExists(fileName):
	if os.path.exists(fileName):
		print 'removing ' + fileName + '...'
		subprocess.call('rm '+fileName, shell=True)

# TODO usuwanie starszych wersji
removeIfExists(outputDBArchive)

removeIfExists(apkOutputFile)
subprocess.call('cp ' + apkSrc + ' ' + apkOutputFile, shell=True)

subprocess.call('zip -r '+outputDBArchive+' GuitarDB -x *.git*', shell=True)

# link do aktualnej wersji
# releaseLinkName = 'SongBook-apkdb-release.zip'
# removeIfExists(releaseLinkName)
# subprocess.call('ln -s ' + outputArchive + ' ' + releaseLinkName, shell=True)

print 'done'