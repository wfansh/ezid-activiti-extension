# Ezid Customized Activiti BPM Engine

A hack into Activiti 5.22 using Maven assembly plugin, to support:

- History process/task variables to be modifiable
- Expose history process/task variables APIs in activiti-rest.war
- Http callback in process end listener as the webhook, with a request failover mechanism

## Running Locally

1. Download [Activiti 5.22](https://github.com/Activiti/Activiti/releases/download/activiti-5.22.0/activiti-5.22.0.zip), extract and put activiti-explorer.war, activiti-rest.war into root folder.
2. Execute maven build command
  ```
  mvn package
  ```
3. Get the customized .war packages in root/target


## How It Works
