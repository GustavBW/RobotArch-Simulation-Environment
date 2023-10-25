import fs, { readFileSync } from 'fs';
import { log, logAndExit } from "./src/logging.js";

log("Process Started");
log("args: " + process.argv);
log("cwd: " + process.cwd());

const argsContainingConfig = [];
process.argv.forEach(function (val, index, array) {
    if (val.includes("=")) {
        argsContainingConfig.push(val);
    }
});

let inputFileName = undefined;
let outputFileName = undefined;
for(let i = 0; i < argsContainingConfig.length; i++){
    const currentStr = argsContainingConfig[i];
    const splitStr = currentStr.split("=");
    if(splitStr[0].includes("--input-file-name")){
        inputFileName = splitStr[1];
    }
    if(splitStr[0].includes("--output-file-name")){
        outputFileName = splitStr[1];
    }
}
if(inputFileName === undefined){
    logAndExit("No input file name provided, exiting", 1);
}
if(outputFileName === undefined){
    logAndExit("No output file name provided, exiting", 1);
}

let input = undefined
try{
    input = readFileSync("../../"+inputFileName, 'utf8');
    log("Successfully read input file")
    log("Input: " + input);
}catch(e){
    log(e);
    logAndExit("Failed to read input file, exiting", 1);
}
const values = input.split(",");
const duration = parseInt(values[0],10);
if(Number.isNaN(duration)){
    logAndExit("Duration {"+duration+"} is not parsable to an integer, exiting", 1);
}
const memoryMB = parseInt(values[1],10);
if(Number.isNaN(memoryMB)){
    logAndExit("MemoryMB {"+memoryMB+"} is not parsable to an integer, exiting", 1);
}

const end = Date.now() + duration;
while(Date.now() < end){
    // Busy wait
    console.log(""); //Sometimes the JVM just skips empty loops, I bet the NodeJS VM does too
}

try{
    fs.writeFileSync("../../"+outputFileName, "BusyWait Complete, waited for " + duration + "ms");
}catch (e){
    log(e);
    logAndExit("Failed to write output file, exiting", 1);
}
logAndExit("Process Complete", 0);

