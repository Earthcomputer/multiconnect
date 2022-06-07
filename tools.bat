@echo off
IF "%1" == "" GOTO NO_ARGS
gradlew :tools:run -q --console=plain "--args=%*"
GOTO END
:NO_ARGS
gradlew :tools:run -q --console=plain
:END
