FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y xvfb default-jre default-jdk xdotool curl
RUN update-alternatives --config java
RUN apt-get install -y libopencv3.2-java
RUN ln -s /usr/lib/jni/libopencv_java320.so /usr/lib/libopencv_java.so
RUN apt-get install -y icewm wmctrl x11vnc
RUN apt-get clean

RUN mkdir tools
RUN pwd
RUN curl -L "http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.7.1/jython-installer-2.7.1.jar" -o /tools/jython_installer-2.7.1.jar
RUN java -jar /tools/jython_installer-2.7.1.jar -s -d jython-2.7.1 -i ensurepip
RUN curl -L "https://raiman.github.io/SikuliX1/sikulixapi.jar" -o /tools/sikulixapi.jar

ADD . /home
WORKDIR "/home"

RUN /jython-2.7.1/bin/jython -m pip install jip
RUN /jython-2.7.1/bin/jython -m pip install -r /home/requirements.txt

RUN chmod +x ./nightly.AppImage
RUN ./nightly.AppImage --appimage-extract
RUN chmod +x /home/squashfs-root/AppRun

EXPOSE 5900
RUN chmod +x start.sh
ENV CLASSPATH=/tools/sikulixapi.jar
ENV DISPLAY=:1
ENTRYPOINT ["./start.sh"]
