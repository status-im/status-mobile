

#ifndef JSIStatus_hpp
#define JSIStatus_hpp

#import <jsi/jsilib.h>
#import <jsi/jsi.h>

using namespace facebook;

void installStatus(jsi::Runtime& jsiRuntime);
void signalStatus(jsi::Runtime& jsiRuntime, std::string signal);
#endif /* JSIStatus_hpp */
