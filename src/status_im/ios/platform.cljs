(ns status-im.ios.platform
  (:require ["react-native" :refer (Dimensions)]))

;; iPhone X dimensions
(def x-height 812)
(def xs-height 896)

(defn iphone-x-dimensions? []
  (let [{:keys [width height]} (-> Dimensions
                                   (.get "window")
                                   (js->clj :keywordize-keys true))]
    (or (= height x-height) (= height xs-height))))

(def platform-specific
  {:status-bar-default-height (if (iphone-x-dimensions?) 0 20)})
