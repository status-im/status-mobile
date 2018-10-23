FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y xvfb default-jre default-jdk xdotool curl
RUN update-alternatives --config java
RUN apt-get install -y libopencv3.2-java
RUN ln -s /usr/lib/jni/libopencv_java320.so /usr/lib/libopencv_java.so
RUN apt-get install -y icewm wmctrl x11vnc
RUN apt-get install -y autoconf automake libtool
RUN apt-get install -y autoconf-archive
RUN apt-get install -y pkg-config
RUN apt-get install -y libpng-dev
RUN apt-get install -y libjpeg8-dev
RUN apt-get install -y libtiff5-dev
RUN apt-get install -y zlib1g-dev
RUN apt-get install -y libicu-dev
RUN apt-get install -y libpango1.0-dev
RUN apt-get install -y libcairo2-dev
RUN apt-get install -y firefox
RUN apt-get clean

RUN mkdir tools
RUN pwd
RUN curl -L "http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.7.1/jython-installer-2.7.1.jar" -o /tools/jython_installer-2.7.1.jar
RUN java -jar /tools/jython_installer-2.7.1.jar -s -d jython-2.7.1 -i ensurepip
RUN curl -L "https://raiman.github.io/SikuliX1/sikulixapi.jar" -o /tools/sikulixapi.jar

RUN curl -L 'http://www.leptonica.com/source/leptonica-1.74.4.tar.gz' -o /home/leptonica-1.74.4.tar.gz
RUN tar -xzf /home/leptonica-1.74.4.tar.gz
RUN ./leptonica-1.74.4/configure
RUN make
RUN make install
RUN curl -L 'https://codeload.github.com/tesseract-ocr/tesseract/tar.gz/3.05.02' -o /home/tess
RUN tar -xzf /home/tess
WORKDIR "tesseract-3.05.02"
RUN ./autogen.sh
RUN ./configure --enable-debug
RUN LDFLAGS="-L/usr/local/lib" CFLAGS="-I/usr/local/include" make
RUN make install
RUN make install-langs
RUN ldconfig

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
