(ns status-im.ios.platform
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

;; iPhone X dimensions
(def x-height 812)
(def xs-height 896)

(defn iphone-x-dimensions? []
  (let [{:keys [width height]} (-> (.-Dimensions rn-dependencies/react-native)
                                   (.get "window")
                                   (js->clj :keywordize-keys true))]
    (or (= height x-height) (= height xs-height))))

(def platform-specific
  {:status-bar-default-height (if (iphone-x-dimensions?) 0 20)})
