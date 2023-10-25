import fs from "fs";
const logFileBuffer = [];
const logDir = "./logs";

/**
 * @returns {string} A formatted date string in the format DD-MM-YYYY-HH-MM-SS-MS
 */
function getFormattedNow() {
    const now = new Date();

    const day = String(now.getDate()).padStart(2, '0');
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const year = now.getFullYear();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    const milliseconds = String(now.getMilliseconds()).padStart(3, '0');

    return `${day}-${month}-${year}-${hours}-${minutes}-${seconds}-${milliseconds}`;
}

/**
 * Dumps the log buffer to a file in the logs directory
 */
const dumpBufferAsFile = () => {
    if(!fs.existsSync(logDir)){
        fs.mkdirSync(logDir);
    }
    const logFileName = logDir + "/log-" + getFormattedNow() + ".txt";
    fs.writeFileSync(logFileName, logFileBuffer.join("\n"));
}

/**
 * 
 * @param {string} message 
 * @param {integer} code 
 */
export function logAndExit (message, code) {
    console.log(message);
    log(message);
    dumpBufferAsFile();
    process.exit(code);
}

/**
 * @param {string} message 
 */
export function log (message) {
    logFileBuffer.push(getFormattedNow() + ": " + message);
}
