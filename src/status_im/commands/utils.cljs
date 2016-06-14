(ns status-im.commands.utils
  (:require [clojure.set :as set]
            [clojure.walk :as w]
            [re-frame.core :refer [register-handler dispatch trim-v debug]]))

(defn json->cljs [json]
  (if (= json "undefined")
    nil
    (js->clj (.parse js/JSON json) :keywordize-keys true)))


(def elements
  {:text        text
   :view        view
   :scroll-view scroll-view
   :image       image
   :touchable   touchable-highlight})

(defn get-element [n]
  (elements (keyword (.toLowerCase n))))

(def events #{:onPress})

(defn wrap-event [event]
  #(dispatch [:suggestions-event! event]))

(defn check-events [m]
  (let [ks  (set (keys m))
        evs (set/intersection ks events)]
    (reduce #(update %1 %2 wrap-event) m evs)))

(defn generate-hiccup [markup]
  ;; todo implement validation
  (w/prewalk
    (fn [el]
      (if (and (vector? el) (string? (first el)))
        (-> el
            (update 0 get-element)
            (update 1 check-events))
        el))
    markup))

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [debug trim-v middleware] handler)))
