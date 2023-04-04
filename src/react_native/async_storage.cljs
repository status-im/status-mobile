(ns react-native.async-storage
  (:require ["@react-native-async-storage/async-storage" :default AsyncStorage]))


(defn set-item [key value]
  (-> (.setItem AsyncStorage (clj->js key) (str value))
      (.then #(println "Data stored successfully!" value))
      (.catch #(js/console.error (str "Error storing data: " %)))))

(defn get-item [key callback]
  (-> (.getItem AsyncStorage (clj->js key))
      (.then (fn [result]
               (callback result)))
      (.catch #(js/console.error (str "Error retrieving data: " %)))))
