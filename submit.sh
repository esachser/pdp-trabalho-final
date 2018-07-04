#!/bin/bash
Flink_home=/home/eduardo/Documentos/PDP/flink-1.5.0
jar_home=./out/artifacts/BigDataBench_Flink_Search_jar
properties_home=./resource


$Flink_home/bin/flink run $jar_home/BigDataBench-Flink-Search.jar $properties_home/Search.properties
