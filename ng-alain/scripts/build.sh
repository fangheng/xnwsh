#!/bin/sh

# go to script directory

cd "${0%/*}"

cd ..


echo "==== starting to build Angular image ===="
# clean cache
npm cache clean --force
# 生成镜像
echo "==== build  Angular Image===="
docker build -f Dockerfile.compile -t ng-app-build .

# 运行编译 Angular 镜像
echo "==== Run  Angular Image===="
docker run --name ng-app-build ng-app-build

# 将 `dist` 复制到项目根目录下
echo "====  copy  Angular Image to this project ===="
docker cp ng-app-build:/usr/src/app/dist ./dist/

# 删除编译 Angular 容器
echo "====  delete Angular contrainer ===="
docker rm -f ng-app-build

# 构建生产环境镜像
echo "==== structure prod Image  ===="
docker build -f  Dockerfile.package -t ng-app .

# 删除 ng-app-build 镜像
echo "====  delete Angular Image ===="
docker rmi -f ng-app-build

echo "==== building completed ===="
