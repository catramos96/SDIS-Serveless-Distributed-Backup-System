@echo off
start rmiregistry
sleep 3
start java cli.TestApp MessageRMI0 BACKUP ../resources/hello.png 2
exit