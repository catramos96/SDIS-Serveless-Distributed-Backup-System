@echo off
start rmiregistry
sleep 5
start java cli.TestApp MessageRMI0 BACKUP test.doc 2
exit