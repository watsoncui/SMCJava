# SMCJava

Usage: 
	java -jar SMCalpha.jar abundance_species_equal.txt
The file contains pair-end sequence
More Usage: 
	java -jar SMCalpha.jar [OPTION] abundance_species_equal.txt
Options:
	-a:	The value followed will be the alpha parameter
	   	Default value 0.0000001
	-n:	The value followed will be the sample number parameter
	   	Default value 100
	-m:	The value followed will be the alpha parameter
	   	Default value 1
	-t:	The value followed will be the threshold parameter
	   	Default value 0.9
	-g:	Choose to use GC order the origin datas first

More and More Usage:

1. default
java -jar SMCalpha.jar abundance_species_equal.txt

2. with parameters
java -jar SMCalpha.jar -n 100 -a 0.001 abundance_species_equal.txt

3. with Java parameters (When out of memory)
java -Xms1024m -jar SMCalpha.jar -n 100 -a 0.001 abundance_species_equal.txt


