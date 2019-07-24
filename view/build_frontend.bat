cd axo-mobile-frontend
call npm install & npm run build & cd .. &^
echo "copy axo-mobile-frontend static resources............" &^
rd /Q/S axo-mobile\src\main\resources\static\ &^
XCOPY axo-mobile-frontend\dist axo-mobile\src\main\resources\static\ /S /E /Y