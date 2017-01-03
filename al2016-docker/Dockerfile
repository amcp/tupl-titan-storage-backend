FROM amazonlinux:latest
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk.x86_64
ENV INSTALL_DIR /usr/local/packages/tupl-titan100-storage-backend-1.0.1-hadoop1
ENV SERVICE_SCRIPT /usr/local/packages/gremlin-server/bin/gremlin-server-service.sh
RUN yum update -y && yum upgrade -y
RUN yum install -y wget java-1.8.0-openjdk-devel git zip unzip
RUN wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
RUN sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
RUN yum update -y && yum upgrade -y
RUN yum install -y apache-maven
RUN alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java
RUN alternatives --set javac /usr/lib/jvm/java-1.8.0-openjdk.x86_64/bin/javac
WORKDIR /tmp
RUN git clone https://github.com/amcp/tupl-titan-storage-backend.git
WORKDIR /tmp/tupl-titan-storage-backend
RUN mkdir -p /usr/local/packages
RUN src/test/resources/install-gremlin-server.sh && mv server/tupl-titan100-storage-backend-1.0.1-hadoop1 /usr/local/packages && rm -rf /tmp/tupl-titan-storage-backend
RUN ln -s ${INSTALL_DIR} /usr/local/packages/gremlin-server
WORKDIR /usr/local/packages/gremlin-server
RUN mkdir -p /var/log/gremlin-server
RUN ln -s /usr/local/packages/gremlin-server /gremlin
EXPOSE 8182
CMD ${INSTALL_DIR}/bin/gremlin-server.sh ${INSTALL_DIR}/conf/gremlin-server/gremlin-server.yaml 1>/var/log/gremlin-server/service.log 2>/var/log/gremlin-server/service.err