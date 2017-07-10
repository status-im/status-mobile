(ns status-im.commands.utils
  (:require [clojure.set :as set]
            [clojure.walk :as w]
            [re-frame.core :refer [dispatch trim-v]]
            [status-im.components.react :as components]
            [status-im.chat.views.input.web-view :as chat-web-view]
            [status-im.chat.views.input.validation-messages :as chat-validation-messages]
            [status-im.chat.views.choosers.choose-contact :as choose-contact]
            [status-im.components.qr-code :as qr]
            [status-im.chat.views.geolocation.views :as geolocation]
            [status-im.utils.handlers :refer [register-handler]]
            [taoensso.timbre :as log]))

(defn json->clj [json]
  (when-not (= json "undefined")
    (js->clj (.parse js/JSON json) :keywordize-keys true)))

(defn parameter-box-separator []
  [components/view {:height 1 :background-color "#c1c7cbb7" :opacity 0.5}])

(def elements
  {:view                 components/view
   :text                 components/text
   :text-input           components/text-input
   :image                components/image
   :qr-code              qr/qr-code
   :linking              components/linking
   :slider               components/slider
   :scroll-view          components/scroll-view
   :web-view             components/web-view
   :touchable            components/touchable-highlight
   :activity-indicator   components/activity-indicator
   :bridged-web-view     chat-web-view/bridged-web-view
   :validation-message   chat-validation-messages/validation-message
   :choose-contact       choose-contact/choose-contact-view
   :separator            parameter-box-separator
   :current-location-map geolocation/current-location-map-view
   :current-location     geolocation/current-location-view
   :places-nearby        geolocation/places-nearby-view
   :places-search        geolocation/places-search
   :dropped-pin          geolocation/dropped-pin})

(defn get-element [n]
  (elements (keyword (.toLowerCase n))))

(def events #{:onPress :onValueChange :onSlidingComplete})

(defn wrap-event [[_ event]]
  (let [data (gensym)]
    #(dispatch [:suggestions-event! (update event 0 keyword) %])))

(defn check-events [m]
  (let [ks (set (keys m))
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

         :else el))
     markup)))

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [trim-v middleware] handler)))
