import fs from "fs";
const logFileBuffer = [];
const logDir = "./logs";

const dumpBufferAsFile = () => {
    if(!fs.existsSync(logDir)){
        fs.mkdirSync(logDir);
    }
    const logFileName = logDir + "/log-" + Date.now() + ".txt";
    fs.writeFileSync(logFileName, logFileBuffer.join("\n"));
}

export function logAndExit (message) {
    console.log(message);
    logFileBuffer.push(message);
    dumpBufferAsFile();
    process.exit();
}

export function log (message) {
    logFileBuffer.push(Date.now() + ": " + message);
}
