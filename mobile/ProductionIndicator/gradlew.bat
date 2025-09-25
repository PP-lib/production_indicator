@ECHO OFF
SETLOCAL
SET APP_BASE_DIR=%~dp0
SET CLASSPATH=%APP_BASE_DIR%gradle\wrapper\gradle-wrapper.jar
IF NOT EXIST %CLASSPATH% (
  ECHO gradle-wrapper.jar not present. Please run 'gradle wrapper' locally.
  EXIT /B 1
)
java -cp %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %*
