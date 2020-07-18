:: WkToRepo.cmd
:: by Mark L 
:: Created on 27 Aug 2017
::
:: Copy source files to local repository
:: It will overwrite existing source files at the local repository

:: Hide messages
@echo off


:: Set the directories names
set Work_Dir=C:\Users\Mark L\eclipse-workspace\HillsSim
set Work_Dir_Src=%Work_Dir%\src
set Work_Dir_Libs=%Work_Dir%\libs
set Work_Dir_Design=%Work_Dir%\design
set Work_Dir_Misc=%Work_Dir%\misc

set Local_Repo=C:\Users\Mark L\Repositories\HillsSim
set Local_Repo_Src=%Local_Repo%\src
set Local_Repo_Libs=%Local_Repo%\libs
set Local_Repo_Design=%Local_Repo%\design
set Local_Repo_Misc=%Local_Repo%\misc

:: Display the options
title WkToRepo - Copy source ^& misc files from Eclipse Workspace to local Repository
echo.
echo WARNING: Operation will overwrite source files at local Repository
echo.
echo To exit, press 'Ctrl+C'. To continue, press any key.
pause >nul
echo.
cls
echo.

:: Copy the files
:: If the file name exists, overwrite after receiving a 'Yes' 

:: Copy src folder
echo Copy %Work_Dir_Src%
xcopy "%Work_Dir_Src%" "%Local_Repo_Src%" /E /S /Y
echo.

echo Copy %Work_Dir_Libs%
xcopy "%Work_Dir_Libs%" "%Local_Repo_Libs%" /E /S /Y
echo.

echo Copy %Work_Dir_Design%
xcopy "%Work_Dir_Design%" "%Local_Repo_Design%" /E /S /Y
echo.

echo Copy %Work_Dir_Misc%
xcopy "%Work_Dir_Misc%" "%Local_Repo_Misc%" /E /S /Y
echo.

:: Display the end message
echo.
echo Operation is complete. Press any key to end
pause >nul
