# Storybook

## Research resources
Configuration is based off multiple projects -
https://github.com/lilactown/storybook-cljs

https://github.com/DavidVujic/clojurescript-amplified

and it is also making use of React Native Web which is the needed ingredient to make it work with Status Mobile project 
https://www.npmjs.com/package/@storybook/addon-react-native-web

https://www.dannyhwilliams.co.uk/introducing-react-native-web-storybook

example repository using it:
https://github.com/dannyhw/addon_react_native_web_example

## Using it for development
if running in a new project or new branch you will probably have to install dependencies first too (make shell -> yarn )

Then: Open two terminal shells -
make storybook-clojure
make storybook-dev

NOTES:

"styles" must be in a `style` block or they will not be picked up. Perhaps we can adjust this in the configurations? Either way it's in the best practices so it's something the codebase is trying to get to.

FIX SVG Imports

Fix Icon imports resolver
