[![progress-banner](https://backend.codecrafters.io/progress/http-server/c4dc2560-2cf9-4c7b-9cf7-cade96ea2eef)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

A simple HTTP 1.1 server built for ["Build Your Own HTTP server"
Challenge](https://app.codecrafters.io/courses/http-server/overview).

### Running

Run `./your-server.sh --directory <path>` to serve files from path (optional).

### Endpoints

- `GET /`: Empty 200 response
- `GET /echo/:value`: 200 response with `value` as the response body
  - Supports `Content-Encoding: gzip` if supplied in the request header
- `GET /user-agent`: 200 response with the value of `User-Agent` as the response body
- `GET /files/:file_name`: 200 response
- `POST /files/:file_name`: 201 response if the file was created successfully
- otherwise 404 response
