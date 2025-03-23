# benchmark notes 

## PR APK Download URLs
* develop - https://status-im-mobile-prs.ams3.cdn.digitaloceanspaces.com/StatusIm-Mobile-250323-103634-f076c9-pr22356-arm64-v8a.apk
* 0fast - https://status-im-mobile-prs.ams3.cdn.digitaloceanspaces.com/StatusIm-Mobile-250313-110702-5a298b-pr22286-arm64-v8a.apk
* 03 - https://status-im-mobile-prs.ams3.cdn.digitaloceanspaces.com/StatusIm-Mobile-250323-184234-d3884e-pr22286-arm64-v8a.apk
* 02 - 

## for develop apk 
```
flashlight test --bundleId im.status.ethereum.pr \
--testCommand "maestro test maestro/benchmarking-flows/main.yml" \
--iterationCount 3 \ 
--resultsTitle "develop" \
--resultsFilePath develop.json
```


## for -0fast apk
```
flashlight test --bundleId im.status.ethereum.pr \
--testCommand "maestro test maestro/benchmarking-flows/main.yml" \
--iterationCount 3 \ 
--resultsTitle "Ofast" \
--resultsFilePath Ofast.json
```

## for -03 apk
```
flashlight test --bundleId im.status.ethereum.pr \
--testCommand "maestro test maestro/benchmarking-flows/main.yml" \
--iterationCount 3 \ 
--resultsTitle "O3" \
--resultsFilePath O3.json
```

## for -02 apk
```
flashlight test --bundleId im.status.ethereum.pr \
--testCommand "maestro test maestro/benchmarking-flows/main.yml" \
--iterationCount 3 \ 
--resultsTitle "O2" \
--resultsFilePath O2.json
```

## to generate comparison HTML
flashlight report develop.json Ofast.json O3.json O2.json
