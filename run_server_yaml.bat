@echo off
setlocal

set "jar=oewn-server-3.0.2-uber.jar"

if not exist "%jar%" (
    echo Non existing uber jar 1>&2
    exit /b 2
)

rem looks for yaml subdirectory in current working directory
rem use -P:model.path=<path>\yaml otherwise
java -jar "%jar%" org.oewntk.json.server.MainKt -P:model.path=yaml_model -P:model.type=yaml %*

endlocal
