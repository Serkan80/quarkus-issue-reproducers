# Multipart Content Type Issue

Start the app:
> mvn quarkus:dev

The app should only allow video formats to be uploaded.

Now upload `pom.xml` and you should expect an error, but it returns OK.

> http -f post :8080/upload file@pom.xml
HTTP/1.1 200 OK
content-length: 0

Or use Swagger UI and you will see the same result.

There is a `MultipartContentTypeFilter` in the code which is commented out. If you enable it, then it works.
