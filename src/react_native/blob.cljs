(ns react-native.blob
  (:require ["react-native-blob-util" :default ReactNativeBlobUtil]
            [react-native.fs :as fs]
            [taoensso.timbre :as log]))

(def temp-image-url (str (fs/cache-dir) "/StatusIm_Image.jpeg"))

(defn get
  [base64-uri trusty on-success]
  (-> (.config ReactNativeBlobUtil
               (clj->js {:trusty trusty
                         :path   temp-image-url}))
      (.fetch "GET" base64-uri)
      (.then #(on-success (.path %)))
      (.catch #(log/error "could not download uri" {:error %}))))
