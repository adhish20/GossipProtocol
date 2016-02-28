#!/bin/bash
TYPE1=2
ALL=4
inputFile="input.txt."
python replicate.py $TYPE1 input.txt
javac -cp .:protobuf.jar GossipInterface.java Server.java
rmiregistry &
for id in `seq 1 $ALL`;
do
	if [ $TYPE1 -gt 0 ]; then
		java -cp .:protobuf.jar Server $id $ALL -i $inputFile$id &
		TYPE1=`expr $TYPE1 - 1`
	else
		java -cp .:protobuf.jar Server $id $ALL &
	fi
done