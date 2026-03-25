@echo off
cd /d "%~dp0"
echo.
echo  UniMind website — local HTTP server
echo  -----------------------------------
echo  1. Start API first:  cd ..\api_server   then   python app.py
echo  2. Keep THIS window open while you use the site.
echo  3. Open: http://127.0.0.1:8765/signup.html  or  signin.html
echo.
start http://127.0.0.1:8765/
python -m http.server 8765
pause
