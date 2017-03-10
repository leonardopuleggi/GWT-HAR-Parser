Small Java app that reads HAR files with GWT RPC requests and attemps to serialize/deserialize the requests/responses.

I had developed this small tool long back to help a couple of devs to check what was happening in our GWT RPC services. GWT is not the latest framework available but I thought I'd share this for other that still need to support GWT applications. It's pretty easy nowadays to obtain an HAR from the client's browser but it might be hard to understand the result, especially for some complex objects contained in the respose.

This tool tries to help you make sense of the request/responses by serializing/deserializing. With the toString() (if you have implemented it) or with Java Reflection in case you haven't implemented your own toString()


## Building

```
mvn clean install
```

## Test with sample file included in the JAR files

```
java -jar target\GWT-HAR-Parser-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Result with sample file

```
-------------------REQUEST 1---Success--------------------

7|0|7|http://127.0.0.1:8083/helloGWT/hellogwt/|5AD3EA1825E71AD70B90AA5DE4AD4A12|com.example.client.GreetingService|greetResult|com.example.shared.HelloResult/3278810364|java.lang.Integer/3438268394|GWT User|1|2|3|4|1|5|5|6|1|7|


toString() =>
com.example.client.GreetingService.greetResult(com.example.shared.HelloResult@32709393)

via reflection =>
com.google.gwt.user.server.rpc.RPCRequest@1f554b06[flags=0,method=public abstract com.example.shared.HelloResult com.example.client.GreetingService.greetResult(com.example.shared.HelloResult),parameters={com.example.shared.HelloResult@32709393},rpcToken=<null>,serializationPolicy=null]


-------------------RESPONSE 1---Success--------------------

//OK[3,1,2,1,["com.example.shared.HelloResult/3278810364","java.lang.Integer/3438268394","GWT User"],0,7]


toString() =>
com.example.shared.HelloResult@6acdbdf5

via reflection =>
com.example.shared.HelloResult@6acdbdf5[a=1,b=GWT User]

```

## Run with your JARs and your HTTP Archive format

```
java -jar target\GWT-HAR-Parser-1.0-SNAPSHOT-jar-with-dependencies.jar C:\temp\libs C:\temp\localhost.har
```
#### Important: you need to pass the JARs that will be used during the serialization/deserialization of your GWT RPC service

This normally means what you have to pass a directory with the JAR files with the `client` and `shared` folder of your GWT project.

If you are using library
to use directly the GWT POJOs from Hibernate POJOs (for example Gilead - https://github.com/emsouza/gilead), please pass also these library JARs.

Ideally you can pass all the UI JARs (`client` and `shared` and `server` that you use)

In case of any problem please contact us
