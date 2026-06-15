@echo off
setlocal
echo ========================================
echo  Rebuilding + Running App...
echo ========================================

set "SRC=D:\QU-N-L-NG-D-NG-B-N-H-NG-TH-NG-MINH-main (1)\QU-N-L-NG-D-NG-B-N-H-NG-TH-NG-MINH-main"
set "M2=C:\Users\HI\.m2\repository"
set "MP=%M2%\org\openjfx\javafx-controls\21.0.6\javafx-controls-21.0.6-win.jar;%M2%\org\openjfx\javafx-base\21.0.6\javafx-base-21.0.6-win.jar;%M2%\org\openjfx\javafx-graphics\21.0.6\javafx-graphics-21.0.6-win.jar;%M2%\org\openjfx\javafx-fxml\21.0.6\javafx-fxml-21.0.6-win.jar;%M2%\org\postgresql\postgresql\42.6.0\postgresql-42.6.0.jar"
set "JAVA=C:\Program Files\BellSoft\LibericaJDK-25\bin\java.exe"

echo.
echo [1/2] Compiling...
powershell -ExecutionPolicy Bypass -File "%SRC%\build.ps1"
if %ERRORLEVEL% NEQ 0 goto :err

echo.
echo [2/2] Launching App...
"%JAVA%" --module-path "%MP%;%SRC%\target\classes" ^
  -m com.example.QUANLYUNGDUNGBANHANG/com.example.QUANLYUNGDUNGBANHANG.Main
goto :end

:err
echo BUILD FAILED.
pause
:end
endlocal
