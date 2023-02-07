#!/usr/bin/bash


pushd .
cd idlelib/ || exit 1
# ./uberdeps/package.sh
# cd ./target
# jar --create  -C classes/ . > idlelib.jar
rm -rf build/
clojure -A:jar
cp target/idlelib-1.0.0-SNAPSHOT.jar ./target/idlelib.jar
popd


[ "$1" = "clj" ] && exit 0

# build roslyn

rm -rf ./bin/RoslynBin
cp -rf ../IdeaIdleRoslyn/out ./bin/RoslynBin

rm -f ./bin/IdeaIdle-*

( gradle buildPlugin && cp -rf ./build/distributions/* ./bin/ ) || exit 1


[ "$1" = "g" ] && git add ./bin && git commit -m "Build"

[ "$1" = "r" ] && emacsclient -n --eval "(idea/rider-playground)"

exit 0
