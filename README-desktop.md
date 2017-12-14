## Build and run lein-figwheel for desktop platform based on react-native-desktop

* git checkout develop-desktop
* lein deps
* npm install
* react-native desktop ( react-native-cli from react-native-desktop should be installed globally )
* ./re-natal use-figwheel
* ./re-natal enable-source-maps ( react-native packager should be patched to load JS sources well on fighweel running )
* In new opened tab: `lein figwheel desktop` ( Runs figwheel to watch and autobuild ClojureScript files into JS )
* In another one new opened tab: `react-native start` ( Starts react-native packager )
* In another one new opened tab( Make sure that node is of v.8 or newer):  
`cd desktop/bin`  
`node ubuntu-server.js`
* In another one new opened tab:
`react-native run-desktop`

## Known issues

* Application with lein-figwheel gets JS errors on first 2 attempts to start, launching fine on 3rd time
* Profile screen automatically gets updated if some core cljs file is modified, for example, `/src/status_im/components/icons/vector_icons.cljs`, but doesn't get updated if `src/status_im/ui/screens/profile/views.cljs` is modified.
* Remote JS Debugging with Chrome Dev Tools environment fails to load because of some JS incompatibility with lein-figwheel, needs investigation.

