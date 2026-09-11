@echo off
REM 简易 Maven 包装脚本（本机 mvn.cmd 在 Git Bash 下无法直接调用时使用）
set MAVEN_HOME=D:\developer\apache-maven-3.8.6
set JAVA_HOME=D:\zen_v1.0\jdk-8u201
"%JAVA_HOME%\bin\java" -classpath "%MAVEN_HOME%\boot\plexus-classworlds-2.6.0.jar" "-Dclassworlds.conf=%MAVEN_HOME%\bin\m2.conf" "-Dmaven.home=%MAVEN_HOME%" "-Dmaven.multiModuleProjectDirectory=%CD%" org.codehaus.plexus.classworlds.launcher.Launcher %*
