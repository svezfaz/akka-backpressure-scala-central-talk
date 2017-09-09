# Back-pressure explained with Akka-Streams and Akka-HTTP

Meetup talk and demo @ [Scala Central Meetup](https://www.meetup.com/Scala-Central/events/235441515/). Online slides [here](https://svezfaz.github.io/akka-backpressure-scala-central-talk/#/).

## Dashboard
```
docker run -d -p 80:80 -p 8125:8125/udp -p 8126:8126 --name kamon-grafana-dashboard kamon/grafana_graphite
```
More info [here](https://hub.docker.com/r/kamon/grafana_graphite/).

## TCP buffers check (on Linux/OS X)
```
netstat -na | grep ${PORT}
```

## Demo
```
sbt run
```
