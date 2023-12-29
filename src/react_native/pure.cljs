(ns react-native.pure
  (:require ["react-native" :refer (AppRegistry View Text SafeAreaView TouchableHighlight FlatList Fragment Pressable)]
            ["react" :refer (useState useEffect) :as React]))

(def view       (.createFactory React View))
(def text       (.createFactory React Text))
(def safe-area  (.createFactory React SafeAreaView))
(def touchable  (.createFactory React TouchableHighlight))
(def flat-list  (.createFactory React FlatList))
(def fragment  (.createFactory React Fragment))
(def pressable  (.createFactory React Pressable))

