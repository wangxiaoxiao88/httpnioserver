#!/bin/sh
mvn clean package
java -cp target/*: me.wangxx.http.NioHttpServer
