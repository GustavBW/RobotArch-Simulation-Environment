package services

import (
	"CloudServer/src/config"
	"bufio"
	"os"
	"path/filepath"
	"strconv"
	"strings"
)

func GetDiskUsage(path string) (int64, error) {
	var totalUsage int64

	err := filepath.Walk(path, func(filePath string, fileInfo os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		if !fileInfo.IsDir() {
			// Get disk usage for this file
			fileUsage := fileInfo.Size()

			// Add to the total usage
			totalUsage += fileUsage
		}

		return nil
	})

	if err != nil {
		return 0, err
	}

	return float32(totalUsage) / float32(config.STORAGE), nil
}

func GetSystemRAMUsage() (float32, error) {
	file, err := os.Open("/proc/meminfo")
	if err != nil {
		return -1, err
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	memInfo := make(map[string]int64)

	for scanner.Scan() {
		line := scanner.Text()
		fields := strings.Fields(line)
		if len(fields) >= 2 {
			key := fields[0]
			value, err := parseAndConvertToInt64(fields[1])
			if err != nil {
				return -1, err
			}
			memInfo[key] = value
		}
	}

	totalRAM := memInfo["MemTotal"]
	var usedRAM int64 = totalRAM - (memInfo["MemFree"] + memInfo["Buffers"] + memInfo["Cached"])

	return float32(usedRAM) / float32(config.MEMORY), nil
}

func GetSystemCPUUsage() (float32, error) {
	file, err := os.Open("/proc/stat")
	if err != nil {
		return 0.0, err
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	var cpuStats [10]int64 // There are 10 values in /proc/stat for CPU stats

	for scanner.Scan() {
		line := scanner.Text()
		fields := strings.Fields(line)

		if len(fields) > 0 && fields[0] == "cpu" {
			for i := 1; i < len(fields); i++ {
				value, err := parseAndConvertToInt64(fields[i])
				if err != nil {
					return 0.0, err
				}
				cpuStats[i-1] = value
			}
			break
		}
	}

	// Calculate the total CPU time (sum of all values)
	totalCPUTime := int64(0)
	for _, value := range cpuStats {
		totalCPUTime += value
	}

	// Calculate the total idle time (sum of idle and iowait values)
	idleTime := cpuStats[3] + cpuStats[4]

	// Calculate the CPU usage percentage
	cpuUsage := 100.0 * (float32(totalCPUTime-idleTime) / float32(totalCPUTime))
	return cpuUsage, nil
}

func parseAndConvertToInt64(s string) (int64, error) {
	value, err := strconv.ParseInt(s, 10, 64)
	if err != nil {
		return 0, err
	}
	return value, nil
}
