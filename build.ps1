$SRC = 'D:\QU-N-L-NG-D-NG-B-N-H-NG-TH-NG-MINH-main (1)\QU-N-L-NG-D-NG-B-N-H-NG-TH-NG-MINH-main'
$M2  = 'C:\Users\HI\.m2\repository'
$MP  = @(
    "$M2\org\openjfx\javafx-controls\21.0.6\javafx-controls-21.0.6-win.jar",
    "$M2\org\openjfx\javafx-base\21.0.6\javafx-base-21.0.6-win.jar",
    "$M2\org\openjfx\javafx-graphics\21.0.6\javafx-graphics-21.0.6-win.jar",
    "$M2\org\openjfx\javafx-fxml\21.0.6\javafx-fxml-21.0.6-win.jar",
    "$M2\org\postgresql\postgresql\42.6.0\postgresql-42.6.0.jar"
) -join ';'

$OUT = "$SRC\target\classes"
New-Item -ItemType Directory -Force -Path $OUT | Out-Null

$files = Get-ChildItem -Path "$SRC\src\main\java" -Filter '*.java' -Recurse |
         Select-Object -ExpandProperty FullName

Write-Host "=== Compiling $($files.Count) files ===" -ForegroundColor Cyan
$args2 = @('-encoding', 'UTF-8', '--module-path', $MP, '-d', $OUT) + $files
$output = & javac @args2 2>&1
$output | ForEach-Object { Write-Host $_ -ForegroundColor Red }

if ($LASTEXITCODE -eq 0) {
    Write-Host "=== BUILD SUCCESS ===" -ForegroundColor Green
} else {
    Write-Host "=== BUILD FAILED ===" -ForegroundColor Red
}
