(ns status-im.utils.utils
  (:require-macros
   [natal-shell.async-storage :refer [get-item set-item]]
   [natal-shell.alert :refer [alert]]
   [natal-shell.toast-android :as toast])
  (:require [status-im.constants :as const]))

(defn require [module]
  (if (exists? js/window)
    (js/require module)
    #js {}))

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
               (str const/server-address action)
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

(defn http-get
  ([url on-success on-error]
   (-> (.fetch js/window url (clj->js {:method "GET"}))
       (.then (fn [response]
                (log response)
                (.text response)))
       (.then on-success)
       (.catch (or on-error
                   (fn [error]
                     (toast (str error))))))))

(defn truncate-str [s max]
  (if (and (< max (count s)) s)
    (str (subs s 0 (- max 3)) "...")
    s))

(defn first-index
  [cond coll]
  (loop [index 0
         cond cond
         coll coll]
    (when (seq coll)
      (if (cond (first coll))
        index
        (recur (inc index) cond (next coll))))))
