# Guassian Blur Process for RobotArch

### Standard Args
As described in https://docs.google.com/document/d/1857-G9cC-_dDsKumEzDjPaAHiHFAO50uuDo-XV2OsfM/edit?usp=sharing <br>
under the segment: Server Processes 

### Additional Args

#### --kernel-size | defult = 10
Kernel size in pixels

#### --threads | default = all of them
How many threads to try and use. 
Duly note that these are software threads and get scheduled as any other instruction. As in: Trying to use more threads as there are physical ones on the machine does not break the process.

### Does 

A cpu based guassian blur algorithm with kernel size --kernel-size using as many threads as possible <br>
Will put the output file at ../../<output-file-name> 

### Requirements
Expects cli args to be --< arg >=< value > - note the equals sign <br>
Expects the input file to be at ../../<input-file-name> <br>
Expects the input file to be an image in png format
