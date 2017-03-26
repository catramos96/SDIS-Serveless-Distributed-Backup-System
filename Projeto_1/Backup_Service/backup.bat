@echo off
start java peer.BackupSystem 1.0 0 8000 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
start java peer.BackupSystem 1.0 1 8000 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
start java peer.BackupSystem 1.0 2 8000 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
start java peer.BackupSystem 1.0 3 8000 224.0.0.117:4446 224.0.0.118:4446 224.0.0.119:4446
timeout /t 2
start java cli.TestApp 8000 BACKUP ../resources/hello.png 2
exit