#include "JSIStatus.hpp"

#include <string>
#include <iostream>

using namespace std;


using namespace facebook;

void installStatus(jsi::Runtime& jsiRuntime){

}

void signalStatus(jsi::Runtime& jsiRuntime, string signal){
    //it works without the signal parameter, but an error with the parameter
    jsiRuntime.global().getPropertyAsFunction(jsiRuntime, "signalFunction").call(jsiRuntime, signal);
}
