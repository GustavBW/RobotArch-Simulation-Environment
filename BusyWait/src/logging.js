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

export function appendAndExit (message) {
    console.log(message);
    logFileBuffer.push(message);
    dumpBufferAsFile();
    process.exit();
}

export function append (message) {
    logFileBuffer.push(Date.now() + ": " + message);
}
