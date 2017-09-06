# 基于哪哥镜像
FROM daocloud.io/java:8
# 拷贝文件到容器
ADD cloud-eureka-server.jar /app.jar
# 配置容器启动后执行的命令
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
