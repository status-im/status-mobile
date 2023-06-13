(ns react-native.syntax-highlighter
  (:require ["react-native" :as react-native]
            ["react-syntax-highlighter" :default Highlighter]))


(defn highlighter
  [props code-string]
  [:> Highlighter
   ;; Default props to adapt Highlighter for react-native.
   (assoc props :Code-tag react-native/View :Pre-tag react-native/View)
   code-string])
