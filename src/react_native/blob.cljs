(ns react-native.blob
  (:require ["react-native-blob-util" :default ReactNativeBlobUtil]
            [taoensso.timbre :as log]))

(defn fetch
  [base64-uri config on-success]
  (-> (.config ReactNativeBlobUtil (clj->js config))
      (.fetch "GET" base64-uri)
      (.then #(on-success (.path %)))
      (.catch #(log/error "could not download uri" {:error %}))))
