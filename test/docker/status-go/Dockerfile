# Build status-go in a Go builder container
FROM golang:1.9-alpine as builder

RUN apk add git --update

ARG build_tags

RUN apk add --no-cache make gcc musl-dev linux-headers

RUN mkdir -p /go/src/github.com/status-im/status-go
RUN git clone http://github.com/status-im/status-go /go/src/github.com/status-im/status-go

RUN cd /go/src/github.com/status-im/status-go && make statusgo BUILD_TAGS="$build_tags"

# Copy the binary to the second image
FROM alpine:latest

RUN apk add --no-cache ca-certificates bash

COPY --from=builder /go/src/github.com/status-im/status-go/build/bin/* /usr/local/bin/

RUN mkdir -p /static/keys
COPY --from=builder /go/src/github.com/status-im/status-go/static/keys/* /static/keys/

# 30304 is used for Discovery v5
EXPOSE 8080 8545 30303 30303/udp 30304/udp

ENTRYPOINT ["/usr/local/bin/statusd"]
