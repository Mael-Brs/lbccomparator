SET proxy_host=""
SET proxy_port=""
java -Dhttp.proxyHost=%proxy_host%  -Dhttp.proxyPort=%proxy_port% -jar .\target\lbc-comparator-spring-boot.jar