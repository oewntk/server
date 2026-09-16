<div style="text-align: center;">
  <img src="images/oewntk.png" alt="OEWNTK" width="256">
</div>
<div style="text-align: center;">
  <img width="150" src="images/mavencentral.png" alt="MavenCentral">
</div>

# OEWN JSON API server

This is a JSON-API based server.
The server loads a model, and listens to clients.
It can load models from YAML or JSON in all formats supported by **fromyaml** and **fromjson** modules.
The recommended format, the fastest to load, is _JSON/model_.
The server is based on the KTOR framework. Ktor is a framework for building asynchronous server-side and client-side applications.
Specifying the expected format is done by passing a _Prefer_ header.

Project [server](https://github.com/oewntk/server)

Project [client](https://github.com/oewntk/client)

# OEWN JSON API

JSON-API based server.

| Request | URL                        | Parameter                                       | Returns               |
|---------|----------------------------|-------------------------------------------------|-----------------------|
| get     | /                          | none                                            | "OEWN"                |      
| get     | /api/synset/{id}           | synsetid                                        | synset                | 
| get     | /api/sense/{id}            | sensekey                                        | sense                 |
| get     | /api/lex/{id}              | lemma,part-of-speech[discriminant]              | lex (unique)          | 
| get     | /api/word/{lemma}          | lemma                                           | collection of lexes   |
| get     | /api/starts/{prefix}       | prefix string                                   | collection of lemmas  | 
| get     | /api/contains/{substring}  | contained string                                | collection of lemmas  |
| get     | /api/matches/lex/{regex}   | regex to match                                  | collection of lemmas  | 
| get     | /api/schema/{schema}       | schema name: (schema\|defs)-(oewn\|data\|model) | schema                |
| get     | /api/schema/{schema class} | schema class name: (oewn\|data\|model)          | dictionary of schemas |

*discriminant* differentiates entries having same part-of-speech but different properties (like pronunciation). It starts with a dash and ends with a number.

## 'Prefer' request header

| Prefer header          | Returns                          |
|------------------------|----------------------------------|
| none                   | model                            |  
| mode=model             | model                            | 
| mode=oewn              | oewn (sense embedded within lex) | 
| mode=data              | flat data                        | 
| mode=data,method=typed | flat typed data                  | 

## Run

An uber-jar is provided (that packs all dependencies) with a _run_server.sh_ script.
Alternatively build a server folder with
- oewn-server-3.0.2-uber.jar
- a _yaml_ subfolder that contains the release of OEWN

Run

`java -jar oewn-server-3.0.2-uber.jar -P:model.path=yaml -P:model.type=yaml
`
## Launch configuration

You can override parameters from application.yaml at runtime by passing -P: arguments to your application jar, or by passing JVM system properties with -D

`java -jar oewn-server-3.0.2-uber.jar -P:ktor.deployment.port=9090
`

`java -jar oewn-server-3.0.2-uber.jar -P:model.path=oewn-model.json -P:model.type=json -P:model.subtype=model
`

`java -jar oewn-server-3.0.2-uber.jar -P:model.path=yaml_model -P:model.type=yaml
`
The relevant parameters are:

| Parameter            | Uses                            | Default         |
|----------------------|---------------------------------|-----------------|
| ktor.deployment.port | the port the server listens on  | 8080            | 
| model.path           | model                           | oewn-model.json |
| model.type           | json \| yaml                    | json            |
| model.subtype        | json format (model\|data\|oewn) | model           |

## Dataflow

![Dataflow](images/dataflow_server_client.png  "Dataflow")

## Maven Central

		<groupId>io.github.oewntk</groupId>
		<artifactId>server</artifactId>
		<version>3.0.2</version>

## Dependencies

![Dependencies](images/server.png  "Dependencies")
