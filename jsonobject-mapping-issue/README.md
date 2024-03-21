# Issue

Quarkus doesn't map `JsonObject`, when it is used in a POST request.

# Reproducer

Start the application with:

> mvn quarkus:dev

Then make the following REST call (with httpie):

> http post :8080/fruits/jsonobject raw '{ "payload": {"name": "john doe"} }'

Result: http 200 with empty body.
Expected: http 200 with the given payload.

When the payload is mapped as `Map<String,Object` then it works:

> http post :8080/fruits/map raw '{ "payload": {"name": "john doe"} }'

Result: http 200 with the given payload.