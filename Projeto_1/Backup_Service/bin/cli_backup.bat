@echo off
start rmiregistry
sleep 5
start java cli.TestApp MessageRMI0 BACKUP ../resources/natural.png 2
exit