(ns utils.worklets.header)

(def ^:private worklets (js/require "../src/js/worklets/header.js"))

(defn header-content-opacity
  [scroll-y threshold]
  (.headerContentOpacity ^js worklets
                         scroll-y
                         threshold))

(defn header-content-position
  [scroll-y threshold top-bar-height]
  (.headerContentPosition ^js worklets
                          scroll-y
                          threshold
                          top-bar-height))
