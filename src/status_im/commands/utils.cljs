(ns status-im.commands.utils
  (:require [clojure.set :as set]
            [clojure.walk :as w]
            [status-im.components.react :as react-components]
            [re-frame.core :refer [dispatch trim-v debug subscribe]]
            [status-im.utils.handlers :refer [register-handler]]
            [clojure.string :as s]))

(def command-prefix "c ")

(defn json->clj [json]
  (when-not (= json "undefined")
    (js->clj (.parse js/JSON json) :keywordize-keys true)))

(def elements
  {:text        react-components/text
   :view        react-components/view
   :slider      react-components/slider
   :scroll-view react-components/scroll-view
   :web-view    react-components/web-view
   :image       react-components/image
   :touchable   react-components/touchable-highlight})

(defn get-element [n]
  (elements (keyword (.toLowerCase n))))

(def events #{:onPress :onValueChange :onSlidingComplete})

(defn wrap-event [[_ event]]
  (let [data (gensym)]
    #(dispatch [:suggestions-event! (update event 0 keyword) %])))

(defn check-events [m]
  (let [ks  (set (keys m))
        evs (set/intersection ks events)]
    (reduce #(update %1 %2 wrap-event) m evs)))

(defn generate-hiccup
  ([markup]
    (generate-hiccup markup {}))
  ([markup data]
   (w/prewalk
     (fn [el]
       (cond

         (and (vector? el) (= "subscribe" (first el)))
         (let [path (mapv keyword (second el))]
           (get-in data path))

         (and (vector? el) (string? (first el)))
         (-> el
             (update 0 get-element)
             (update 1 check-events))

         :esle el))
     markup)))

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [trim-v middleware] handler)))
