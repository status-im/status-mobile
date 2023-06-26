# Component Tests

The component tests are using React Native Testing Library - https://callstack.github.io/react-native-testing-library/
and Jest - https://jestjs.io/


It is highly recommended to read some advice from Kent C.Dodds on how to write tests and use these tools correctly.


https://kentcdodds.com/blog/common-mistakes-with-react-testing-library

https://www.youtube.com/watch?v=ahrvE062Kv4

Both of these links are showing it for React-Testing-Library (not Native) however the approach is for the most part considered the same.

## Running the tests
To run these tests there are two methods.

`make component-test` 
setups and runs the test suite once.

`make component-test-watch` 
setups and runs the test suite and watches for code changes will then retrigger the test suite.

## Writing Tests
New test files will need their namespace added to either the file "src/quo2/core_spec.cljs" or "src/status_im2/core_spec.cljs. These locations may update overtime but it is dependent on the entrypoint in shadow-cljs config discussed below.


### Best practices
For the moment we will keep best practices for tests in our other guidelines document:

To that point these guidelines will follow the conventions of Jest and React Native Testing Library recommendations and Status mobile will just stack their preferences on top.

### Utilities
There is a file of utility functions defined in "src/test_helpers/component.cljs" and "src/test_helpers/component.clj". It will be great to use these utilities and to add any common testing tools to these files as it should make writing tests easier and faster.


## Configuration
Status Mobile has a bespoke tech stack, as such there is more complexities to configuring the tests.

### Shadow-CLJS 
the configuration for compiling our tests are defined in the "shadow-cljs.edn" file.
The three main parts of this are
`:target :npm-module`
Needed for the configuration we are using
`:entries`
a vector of entry points for the test files.
and the `ns-regexp` to specify what tests to find. Since we have multiple forms of tests we decided that "component-spec" is the least likely to detect the wrong file type.

It's worth knowing that our tests are compiled to JS and then run in the temporary folder `component-tests`. 

### Jest
There is also further configuration for Jest in "test/jest". There is a jest config file which has some mostly standard configuration pieces, where the tests live, what environment variables are set etc. This is documented by Jest here: https://jestjs.io/docs/configuration

There is also a setup file which is used to set some global and default values. Additionally this file is used to mock some of the react native (among other) dependencies 
