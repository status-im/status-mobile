# Descirption

This is a base Docker image used by all other images used for building the Status app.

# Packages

It includes:

* Generic utilities: `file`, `zip`, `unzip`, `curl`, `wget`, `s3cmd`
* Interpreters and compilters: `nodejs`, `java`
* Package and build mangers: `yarn`, `nvm`, `leiningen`
* User `jenkins` for use with Jenkins continuous integration system
