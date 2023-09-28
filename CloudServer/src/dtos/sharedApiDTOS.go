package dtos

import "time"

type ServerAction struct {
	Method string `json:"method"`
	Url    string `json:"url"`
	Ip     string `json:"ip"`
	Port   int16  `json:"port"`
}

type ServerSpecification struct {
	Irn     string  `json:"irn"`
	Memory  int32   `json:"memory"`
	Cpus    float32 `json:"cpus"`
	Storage int32   `json:"storage"`
	Latency int32   `json:"latency"`
}

type SystemUtilization struct {
	Cpu  float32 `json:"cpu"`
	Mem  float32 `json:"mem"`
	Disk float32 `json:"disk"`
}

type ServerMetadata struct {
	Id            int64               `json:"id"`
	Actions       []ServerAction      `json:"actions"`
	Specification ServerSpecification `json:"specification"`
	Utilization   SystemUtilization   `json:"utilization"`
}

type ProcessResults struct {
	ComputeMS int64     `json:"computeMs"`
	Start     time.Time `json:"start"`
	Result    string    `json:"result"`
}
