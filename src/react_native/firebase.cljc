(ns react-native.firebase
  (:require
    #?@(:fdroid
          [[react-native.firebase.firebase-fdroid]]
        :google
          [[react-native.firebase.firebase-google]]
        :cljs
          [[react-native.firebase.firebase-unsupported]])))

#?(:fdroid
     [(def request-remote-token react-native.firebase.firebase-fdroid/request-remote-token)
      (def revoke-remote-token react-native.firebase.firebase-fdroid/revoke-remote-token)]
   :google
     [(def request-remote-token react-native.firebase.firebase-google/request-remote-token)
      (def revoke-remote-token react-native.firebase.firebase-google/revoke-remote-token)]
   :cljs
     [(def request-remote-token react-native.firebase.firebase-unsupported/request-remote-token)
      (def revoke-remote-token react-native.firebase.firebase-unsupported/revoke-remote-token)])
