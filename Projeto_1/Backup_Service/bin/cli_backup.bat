@echo off
start rmiregistry
sleep 5
start java cli.TestApp MessageRMI0 BACKUPENH ../resources/natural.png 3
exit