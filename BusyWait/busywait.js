import { readFileSync } from 'fs';
import { append, appendAndExit } from "./src/logging.js";

append("Process Started");
append("args: " + process.argv);

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
    appendAndExit("No input file name provided, exiting");
}
if(outputFileName === undefined){
    appendAndExit("No output file name provided, exiting");
}

const input = undefined
try{
    input = readFileSync("../"+inputFileName+".csv", 'utf8');
}catch(e){
    appendAndExit("Failed to read input file, exiting");
}
const values = input.split(",");
const duration = values[0];
if(!Number.isInteger(duration)){
    appendAndExit("Duration is not an integer, exiting");
}

console.log("Not Implimented");