(ns messenger.android.utils
  (:require-macros
   [natal-shell.async-storage :refer [get-item set-item]]
   [natal-shell.alert :refer [alert]]
   [natal-shell.toast-android :as toast]))

(def server-address "http://rpc0.syng.im:20000/")

(defn log [obj]
  (.log js/console obj))

(defn toast [s]
  (toast/show s (toast/long)))

(defn on-error [error]
  (toast (str "error: " error)))

(defn http-post
  ([action data on-success]
   (http-post action data on-success nil))
  ([action data on-success on-error]
   (-> (.fetch js/window
               (str server-address action)
               (clj->js {:method "POST"
                         :headers {:accept "application/json"
                                   :content-type "application/json"}
                         :body (.stringify js/JSON (clj->js data))}))
       (.then (fn [response]
                (log response)
                (.text response)))
       (.then (fn [text]
                (let [json (.parse js/JSON text)
                      obj (js->clj json :keywordize-keys true)]
                  (on-success obj))))
       (.catch (or on-error
                   (fn [error]
                     (toast (str error))))))))
