#pragma once

#include <jsi/jsi.h>

using namespace facebook::jsi;

class HelloWorldJSI {
public:
    static void install(Runtime& runtime);

private:
    static void helloWorld(Runtime& runtime, Value thisValue, Value* arguments, size_t count);
};
