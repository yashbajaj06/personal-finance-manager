@ECHO OFF
SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

SET "BASE_DIR=%~dp0"
IF "%BASE_DIR:~-1%"=="\" SET "BASE_DIR=%BASE_DIR:~0,-1%"

SET "WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper"
SET "WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties"
SET "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"

IF NOT "%JAVA_HOME%"=="" (
  SET "JAVA_HOME_TRIMMED=%JAVA_HOME%"
  IF "!JAVA_HOME_TRIMMED:~-1!"=="\" SET "JAVA_HOME_TRIMMED=!JAVA_HOME_TRIMMED:~0,-1!"
  SET "JAVA_EXE=!JAVA_HOME_TRIMMED!\bin\java.exe"
  IF NOT EXIST "!JAVA_EXE!" (
    ECHO Error: JAVA_HOME is set to "%JAVA_HOME%" but "!JAVA_EXE!" does not exist.
    ECHO Check that JAVA_HOME points to a valid JDK installation folder.
    EXIT /B 1
  )
) ELSE (
  WHERE java.exe >NUL 2>NUL
  IF ERRORLEVEL 1 (
    ECHO Error: JAVA_HOME is not set and no 'java' command could be found on the PATH.
    ECHO Install Java 17+ and make sure it is on your PATH, then try again.
    EXIT /B 1
  )
  SET "JAVA_EXE=java.exe"
)

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Downloading Maven Wrapper...
  FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="wrapperUrl" SET "WRAPPER_URL=%%B"
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%'"
  IF ERRORLEVEL 1 (
    ECHO Error: failed to download the Maven Wrapper jar from !WRAPPER_URL!
    EXIT /B 1
  )
)

"!JAVA_EXE!" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

ENDLOCAL
