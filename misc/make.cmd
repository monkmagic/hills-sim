:: 'Make' file for the HillsSim Program

@echo off

:: -----------------------
:: Declare the directories
:: -----------------------

set WORK_DIR=C:\Users\Mark L\Desktop\Active\HillsSim
set SRC_DIR=C:\Users\Mark L\eclipse-workspace\HillsSim
set JAVA_DIR=%WORK_DIR%\com\hills\sim
set LIB_DIR=%WORK_DIR%\libs


:: ---------------------
:: Copy non-source files
:: ---------------------

:: Copy the html settings file and overwrite any existing file
xcopy /y "%SRC_DIR%\misc\*.html" "%WORK_DIR%"


:: ------------------------------
:: Copy java source and jar files
:: ------------------------------

:: Create the directory to contain the source file
mkdir "%JAVA_DIR%"
mkdir "%LIB_DIR%"


:: Copy the jar and java files over
copy /Y "%SRC_DIR%\libs\*.jar" "%LIB_DIR%"
xcopy /e "%SRC_DIR%\src\com\hills\sim" "%JAVA_DIR%"


:: -------------------
:: Compile the program
:: -------------------

:: Compile 'Indis' packages
javac -cp ".;libs/*" "%JAVA_DIR%"\indis\*.java 


:: Compile 'EA' packages
javac -cp ".;libs/*" "%JAVA_DIR%"\ea\*.java 
javac -cp ".;libs/*" "%JAVA_DIR%"\ea\prototype\*.java 
javac -cp ".;libs/*" "%JAVA_DIR%"\ea\teststrategy\*.java 


:: Compile main program
:: Compile 'Exceptions' packages
:: Compile 'Logs' packages
:: Compile 'Settings' packages
:: Compile 'Stage' packages
:: Compile 'Worker' packages

javac -cp ".;libs/*" "%JAVA_DIR%"\*.java 
javac -cp ".;libs/*" "%JAVA_DIR%"\exceptions\*.java
javac -cp ".;libs/*" "%JAVA_DIR%"\logs\*.java
javac -cp ".;libs/*" "%JAVA_DIR%"\settings\*.java
javac -cp ".;libs/*" "%JAVA_DIR%"\stage\*.java
javac -cp ".;libs/*" "%JAVA_DIR%"\worker\*.java

:: Create executable jar file
jar cfe sim.jar com.hills.sim.SimEngine com\


:: ------------
:: Remove files
:: ------------

:: Extracted directories
rmdir /S /q com\
