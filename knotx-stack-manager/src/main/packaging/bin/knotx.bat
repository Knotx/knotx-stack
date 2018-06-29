@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  vertx startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and VERTX_OPTS to pass JVM options to this script.
set JVM_OPTS=-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0

@rem To enable JMX uncomment the following
@rem JMX_OPTS=-Dcom.sun.management.jmxremote -Dhazelcast.jmx=true -Dvertx.options.jmxEnabled=true -Dvertx.metrics.options.jmxDomain=knotx

@rem enable remote debug port, uncomment the following
@rem set JVM_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=18092

@rem You can specify the path to the vertx cache directory. If not specified a %KNOTX_HOME%\.vertx is used
@rem set VERTX_CACHE_DIR=%KNOTX_HOME%/.vertx

@rem You can specify path to your custom logger configuration file. If not specified a %KNOTX_HOME%\conf\logback.xml is used
@rem set KNOTX_LOGBACK_CONFIG=

@rem You can specify path to your custom Hazelcast cluser.xml file. If not specified a %KNOTX_HOME%\conf\default-cluster.xml is used
@rem set CLUSTER_CONFIG=

@rem You can specify hazelcast cluster options here. See http://docs.hazelcast.org/docs/3.6/manual/html-single/index.html#system-properties for the available options.
set HAZELCAST_OPTS=-Dhazelcast.max.no.heartbeat.seconds=5

@rem You can enable Vert.x metrics by uncommenting this line.
@rem METRICS_OPTS=-Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=knotx-dropwizard-registry

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line
if "%KNOTX_HOME%" == "" set KNOTX_HOME=.
if "%KNOTX_LOGBACK_CONFIG%" == "" set KNOTX_LOGBACK_CONFIG=%KNOTX_HOME%\conf\logback.xml
if "%CLUSTER_CONFIG%" == "" set CLUSTER_CONFIG=%KNOTX_HOME%\conf\default-cluster.xml
if "%VERTX_CACHE_DIR%" == "" set VERTX_CACHE_DIR=%KNOTX_HOME%\.vertx

set CLASSPATH=%CLASSPATH%;%KNOTX_HOME%\conf;%KNOTX_HOME%\lib\*

@rem Execute vertx
"%JAVA_EXE%" %JVM_OPTS% %JAVA_OPTS% %JVM_DEBUG% ^
  -Dlogback.configurationFile=%KNOTX_LOGBACK_CONFIG% ^
  -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory ^
  -Dhazelcast.logging.type=slf4j ^
  -Dvertx.clusterManagerFactory=io.vertx.spi.cluster.impl.hazelcast.HazelcastClusterManagerFactory ^
  -Dvertx.hazelcast.config=%CLUSTER_CONFIG% ^
  -Dknotx.home=%KNOTX_HOME% ^
  -Dvertx.cacheDirBase=%VERTX_CACHE_DIR% ^
  -Dvertx.cli.usage.prefix=knotx ^
  -classpath %CLASSPATH% ^
  io.vertx.core.Launcher %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable VERTX_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%VERTX_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
