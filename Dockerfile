#FROM registry.namely.land/namely/sbt:1.3.6-2.13.1 as build
FROM mozilla/sbt
COPY . .
ENTRYPOINT sbt run
