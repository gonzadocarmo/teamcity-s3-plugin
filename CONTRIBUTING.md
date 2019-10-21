# Contributing: Extending the Plugin

## How to Build

- ### Requisites
  - Java 7
  - Maven

- ### Command

        mvn clean package

## How to test the plugin locally

- First, build according to instructions above.
- Secondly, use some docker image to run the Teamcity Server:

        docker run -it --name teamcity-server-instance  \
           -v /tmp:/data/teamcity_server/datadir \
           -v /tmp:/opt/teamcity/logs  \
           -p 9999:8111 \
           jetbrains/teamcity-server:9.1.7

- Open the browser and navigate to `"http://localhost:9999"` (port defined on line above)
- Upload the plugin:
    - via browser:
        - click on `Administration` --> `Plugins List` --> `Upload plugin zip` --> `Choose file` (file located under target/awsS3Plugin-x.y.x.zip") --> `Save`
    - via docker image:

            docker cp target/awsS3Plugin-x.y.x.zip teamcity-server-instance:/data/teamcity_server/datadir/plugins/
- restart the server
    - via docker image:

            docker exec -it teamcity-server-instance /bin/bash
            /opt/teamcity/bin/runAll.sh stop
            /opt/teamcity/bin/runAll.sh start

- Open the browser and navigate again to `"http://localhost:9999"`, create a job with this runner and test it!

- Logs could be found at:

        /opt/teamcity/logs/teamcity-server.log
        /opt/teamcity/buildAgent/logs/teamcity-agent.log