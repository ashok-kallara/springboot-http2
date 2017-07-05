#!/bin/bash

#package it up
readonly usage="
Make new Tomcat native image with gradle and optionally publish it.\n
Usage arguments:\n
    example: ./build.sh -v 1.0.0 -r docker.io -p\n
    -v|--version Version number of the image. Ex: 1.0.0.\n
    -r|--registry Docker registry URL.\n
    -p|--publish (Optional) Publish the image to the supplied docker registry.\n
    --release (Optional) Release tag.\n
    --latest (Optional) Move the latest tag.\n"


while test $# -gt 0; do
     case "$1" in
        -h|--help )
            echo  -e "$usage"
            exit 0
            ;;
        -v|--version )
            shift
            if test $# -gt 0; then
                version=$1
            fi
            shift
            ;;
        -r|--registry )
            shift
            if test $# -gt 0; then
                registryUrl=$1
            fi
            shift
            ;;
        -p|--publish )
            push=1
            shift
            ;;
        --latest )
            latest=1
            shift
            ;;
        --release )
            release=1
            shift
            ;;
        * )
            break
            ;;
    esac
done

if [ -z ${version+x} ]; then
    echo "--version param is required."
    echo -e $usage
    exit 1
fi

if [ -z ${registryUrl+x} ]; then
    echo "--registry param is required."
    echo -e $usage
    exit 1
fi

function build.build_image() {
    local tag="$1"

    docker build --build-arg BUILD_DATE_ARG="${build_date}" --build-arg VERSION_ARG="${version}" --build-arg BUILD_ARG="${build}" \
        -t ${registryUrl}/tomcatnative-gradle:$tag .
}

function build.push_image() {
    local tag="$1"
    if [ "${push}" == "1" ]; then
        docker push ${registryUrl}/tomcatnative-gradle:$tag
    fi
}

function build.tag_image() {
    local fromTag="$1"
    local toTag="$2"

    docker tag ${registryUrl}/tomcatnative-gradle:$fromTag ${registryUrl}/tomcatnative-gradle:$toTag
}

build="$(date +%s)"
build_date="$(date)"

build.build_image $version
build.push_image $version

echo "latest: $latest"
echo "release: $release"

if [ "${latest}" == "1" ]; then
    build.tag_image $version "latest"
    build.push_image "latest"
fi

if [ "${release}" == "1" ]; then
    build.tag_image $version "release"
    build.push_image "release"

    build.tag_image $version "latest"
    build.push_image "latest"
fi


