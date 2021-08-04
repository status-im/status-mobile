#include "JSIStatus.hpp"

#include <string>
#include <iostream>

using namespace std;


using namespace facebook;

void installStatus(jsi::Runtime& jsiRuntime){

}

void signalStatus(jsi::Runtime& jsiRuntime, string signal){
    cout << signal;
    jsiRuntime.global().getPropertyAsFunction(jsiRuntime, "signalFunction").call(jsiRuntime, signal);
}
