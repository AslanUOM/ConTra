
                          Contra Service

  What is it?
  -----------
  The contraservice is JAX-RS webservice API for mobile applications. It can be deployed
  in any servlet containers and accessed through HTTP requests. This module receives the
  requests from mobile devices, send it to the contramodel and or CEP and returns the
  response back to the mobile device.


  How to install?
  ---------------
  1. Create the JAR file using the following command from the ConTra directory.

        mvn package

  If you wat to package the contraservice only, use the following command from the ConTra directory.

        mvn -pl contraservice -am package

  2. Copy the contraservice/targets/contra.war file to {CATALINA_HOME}/webapps

  3. Start the Apache Tomcat

        Log is printed to {CATALINA_HOME}/logs/contra.log file


  How to use?
  -----------
  Send HTTP requests to http://localhost:8080/contra/service/{resource} with necessary details.

  Using PostMan:
    # URL:              http://localhost:7474/contra/user/find/+94771234567
    # METHOD:           GET