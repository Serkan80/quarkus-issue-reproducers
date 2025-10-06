# Issue

When a "compact cookie" is sent with multiple values in a request, then `@RestCookie` as well as `@CookieParam` don't work.

They only work when 

# Reproducer

Start the application with:

> mvn quarkus:dev

Then make the following REST call (with httpie):

> http post :8080/fruits Cookie:'value1=1;value2=2'

Result: http 200 with empty body.  
Expected: http 200 with the given payload.

When the payload is mapped as `Map<String,Object` then it works:

> http post :8080/fruits/map raw '{ "payload": {"name": "john doe"} }'

Result: http 200 with the given payload.
