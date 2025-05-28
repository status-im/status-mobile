(ns react-native.webview
  (:require
    ["react-native-webview" :default rn-webview]
    [reagent.core :as reagent]
    [utils.transforms :as transforms]))

(def view (reagent/adapt-react-class rn-webview))

(defn go-back
  [^js webview-ref]
  (some-> ^js webview-ref
          (.goBack)))

(defn go-forward
  [^js webview-ref]
  (some-> ^js webview-ref
          (.goForward)))

(defn inject-js
  [^js webview-ref js-script]
  (some-> webview-ref
          (.injectJavaScript js-script)))

(defn post-message
  [^js webview-ref message]
  (some-> webview-ref
          (.postMessage (-> message transforms/clj->json))))
