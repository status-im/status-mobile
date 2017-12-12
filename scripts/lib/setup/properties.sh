#!/usr/bin/env bash

function property() {
    grep "${2}" ${1}|cut -d'=' -f2
}

function property_gradle() {
    property $(repo_path)/android/gradle.properties ${1}
}