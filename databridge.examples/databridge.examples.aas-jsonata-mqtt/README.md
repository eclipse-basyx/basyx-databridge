## "examples.aas-jsonata-mqtt" based on newly implemented **AAS Consumer**

- **AAS Consumer** consume data from data source and send these data to **Data Sink (MQTT Broker)** via **JSONata** transformer

- There are two types use case based on `aasserver_datasource.json` configuration
  - With properties (Such as `pressure`, `rotation`)
  - Without properties
 
- With properties basic workflow

![Work Flow -1](https://github.com/masud-svg/demo-photo/blob/master/Capture.PNG)

- Without properties basic workflow

![Work Flow -2](https://github.com/masud-svg/demo-photo/blob/master/Capture1.PNG)

- *In order to run without properties work flow, required configuration file exists in  `databridge.examples/databridge.examples.aas-jsonata-mqtt/src/main/resources/testcase-2`*
