## Containerized Springboot on Tomcat native (with support for HTTP/2)

A proof-of-concept to run Springboot applications Debian based container with support for:
* Tomcat native with APR over SSL
* HTTP/2

### Build tomcat-native/Gradle image
[Dockerfile](Dockerfile) is based on Tomcat's Dockerfile with the following modifications:
  * Changed `TOMCAT_MAJOR` and `TOMCAT_VERSION` to match Springboot tomcat container version
  * Changed URL for Tomcat gpg keys
  * Added `Gradle-3.3` SDK
  * Removed clean-up of JDK for the convinience of building and running inside container (`gradle bootRun`)

To build, run `./build.sh`. Run `build.sh -h` to see available options. It generates an image <code><b><i><REGISTRY_URL></i>/tomcatnative-gradle</b></code>. Image will be tagged with a version pulled from gradle project

example: <code>./build.sh -r docker.io -p</code>
```
  -v|--version Version number of the image. e.g: 1.0.0
  -r|--registry Docker registry URL.
  -p|--publish (Optional) Publish the image to the supplied docker registry.
  --release (Optional) Release tag.
  --latest (Optional) Move the latest tag.
```

__NOTE:__ Building the image will not package `springboot-http2` project inside it. Refer __Usage__ section for how to run the project.
<br>

### Usage
#### Run with `gradle bootRun`
build.gradle has `bootRun` task defined with all required params including ALPN library required to have http/2 working on JDK-8.
Run the following from `sprintboot-http2` project directory:<br>
<code>docker run -d -p 8849:8849 -p <b>9900:9900</b> -p <b>9910:9910</b> -p <b>9920:9920</b> -v /${HOME}/.gradle:/gradle-cache -v ${PWD}:/springboot-http2 --name springboot-http2 <i><REGISTRY_URL></i>/tomcatnative-gradle <b>bash -c "cd /springboot-http2 && gradle bootRun"</b></code>

    * 8849 --> JMX port for profiling (optional)
    * 9900 --> http/2 port (h2 with SSL)
    * 9910 --> NIO connector with SSL
    * 9920 --> APR connector with SSL

#### Run as executable jar
  1. Run `gradle bootRepackage`. This will generate a jar `build/libs/http2-poc-0.0.1-SNAPSHOT.jar`
  2. Docker run with bash.
  3. Run with alpn jar in classpath as shown in [start.sh](start.sh) from inside the container.


