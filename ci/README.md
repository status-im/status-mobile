# Description

This folder contains files defininf [Jenkins pipelines](https://jenkins.io/doc/book/pipeline/) that run on https://ci.status.im/.

# Libraries

All `Jenkinsfile`s contain the following line:
```groovy
library 'status-react-jenkins@master'
```

Which loads the used methods - like `nix.shell()` - from a separate private repo:

https://github.com/status-im/status-react-jenkins

This is done to improve security of our CI setup.
