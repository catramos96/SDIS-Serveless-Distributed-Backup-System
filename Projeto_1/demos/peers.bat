@echo off
start rmiregistry
sleep 3
start java peer.BackupSystem 1.1 0 MessageRMI0 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
start java peer.BackupSystem 1.1 1 MessageRMI1 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
start java peer.BackupSystem 1.1 2 MessageRMI2 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
start java peer.BackupSystem 1.1 3 MessageRMI3 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
exit