@echo off
start rmiregistry
timeout /t 3
start java peer.BackupSystem 1.0 0 MessageRMI0 224.0.1.117:4446 224.0.1.118:4446 224.0.1.119:4446
start java peer.BackupSystem 1.0 1 MessageRMI1 224.0.1.117:4446 224.0.1.118:4446 224.0.1.119:4446
start java peer.BackupSystem 1.0 2 MessageRMI2 224.0.1.117:4446 224.0.1.118:4446 224.0.1.119:4446
exit