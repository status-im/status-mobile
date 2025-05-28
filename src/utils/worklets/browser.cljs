(ns utils.worklets.browser
  (:require [goog.object :as gobj]
            [react-native.utils :as utils]))

(def ^:private worklets (js/require "../src/js/worklets/browser.js"))

(defn- transform-args
  [f]
  (fn [& args]
    (apply f (map utils/kebab-case-map->camelCase-obj args))))

(defn- worklet-wrapper
  [worklet-name]
  (if-let [worklet-fn (gobj/get worklets worklet-name)]
    (transform-args worklet-fn)
    (throw (js/Error.
            (ex-info "Non-existing worklet!"
                     {:name worklet-name
                      :file "../src/js/worklets/browser.js"})))))

(def use-scroll-tab (worklet-wrapper "useScrollTab"))
