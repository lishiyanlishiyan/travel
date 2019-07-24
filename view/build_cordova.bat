cd axo-mobile-frontend
call npm install & npm run build-cordova & cd .. &^
echo "copy axo-mobile-frontend resources to axo-mobile-app............" &^
rd /Q/S axo-mobile-app\www\ &^
XCOPY axo-mobile-frontend\dist axo-mobile-app\www\ /S /E /Y