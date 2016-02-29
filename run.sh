#!/bin/bash
Args=( "$@" )
TYPE1=5
ALL=10
inputFile="input.txt"

if [ $# -ne 0 ]; then
	for var in `seq 1 $#`;
	do
		if [ ${Args[`expr $var - 1`]} = "-p" ]; then
			TYPE1=${Args[$var]}
		elif [ ${Args[`expr $var - 1`]} = "-n" ]; then
			ALL=${Args[$var]}
		elif [ ${Args[`expr $var - 1`]} = "-i" ]; then
			inputFile=${Args[$var]}
		fi
	done
fi

if [ $TYPE1 -le $ALL ]; then
	killall java
	killall rmiregistry
	python replicate.py $TYPE1 $inputFile
	javac -cp .:protobuf.jar GossipInterface.java Server.java
	rmiregistry &
	for id in `seq 1 $ALL`;
	do
		if [ $TYPE1 -gt 0 ]; then
			java -cp .:protobuf.jar Server $id $ALL -i $inputFile.$id &
			TYPE1=`expr $TYPE1 - 1`
		else
			java -cp .:protobuf.jar Server $id $ALL &
		fi
	done
else
	echo "TYPE-1 Process must be less than or equal to Total Processes"
fi