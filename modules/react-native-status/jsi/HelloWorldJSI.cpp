#include "HelloWorldJSI.h"
#include <jsi/jsi.h>
#include <iostream>

using namespace facebook::jsi;

void HelloWorldJSI::install(Runtime& runtime) {
    auto helloWorldFunction = Function::createFromHostFunction(runtime,
        PropNameID::forAscii(runtime, "helloWorld"),
        0,
        [](Runtime& runtime, Value thisValue, Value* arguments, size_t count) -> Value {
            std::cout << "Hello, World! from C++" << std::endl;
            return Value::undefined();
        });

    runtime.global().setProperty(runtime, "helloWorld", std::move(helloWorldFunction));
}
