import sys

def replicate(n, input):
	for i in range(1,(int(n)+1)):
		inFile = open(input,'r')
		outFile = open("input.txt."+str(i),'w')
		for line in inFile:
			outFile.write(str(i)+":"+line)
		outFile.close()
		inFile.close()


if __name__ == '__main__':
	replicate(sys.argv[1], sys.argv[2])