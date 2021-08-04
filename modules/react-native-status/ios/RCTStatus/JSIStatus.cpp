#include "JSIStatus.hpp"

#include <string>
#include <iostream>

using namespace std;


using namespace facebook;

void installStatus(jsi::Runtime& jsiRuntime){

}


void signalStatus(jsi::Runtime& jsiRuntime, string signal){
    string strMytestString("hello world");
        cout << strMytestString;
    cout << signal;
    //hwo to invoke JS function ?
    //jsiRuntime.global().getFunction(jsiRuntime, "signalFunction").call(runtime, signal);
}
