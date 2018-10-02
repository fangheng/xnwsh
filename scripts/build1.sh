#!/bin/sh

# Put the environment variables here

# go to script directory
cd "${0%/*}"

cd ..

# package all the sub-projects
#echo "==== starting to build all the sub-projects ===="
# ./gradlew clean
# ./gradlew bootRepackage -Pprod -PnodeInstall -x test

# echo "==== building docker images ===="
# ./gradlew buildDocker -x test

# echo "==== building completed ===="

cd ngAlain
echo "==== building npm install===="
npm install 
echo "==== building npm start===="
npm start
#test
