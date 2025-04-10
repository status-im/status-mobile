(ns utils.worklets.communities
  (:require [goog.object :as gobj]
            [react-native.utils :as utils]))

(def ^:private worklets (js/require "../src/js/worklets/communities.js"))

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
                      :file "../src/js/worklets/communities.js"})))))

(def use-logo-styles (worklet-wrapper "useLogoStyles"))
(def use-sheet-styles (worklet-wrapper "useSheetStyles"))
(def use-name-styles (worklet-wrapper "useNameStyles"))
(def use-info-styles (worklet-wrapper "useInfoStyles"))
(def use-channels-styles (worklet-wrapper "useChannelsStyles"))
(def use-scroll-to (worklet-wrapper "useScrollTo"))
(def use-header-opacity (worklet-wrapper "useHeaderOpacity"))
(def use-opposite-header-opacity (worklet-wrapper "useOppositeHeaderOpacity"))
(def use-nav-content-opacity (worklet-wrapper "useNavContentOpacity"))

(def on-pan-start (worklet-wrapper "onPanStart"))
(def on-pan-update (worklet-wrapper "onPanUpdate"))
(def on-pan-end (worklet-wrapper "onPanEnd"))
