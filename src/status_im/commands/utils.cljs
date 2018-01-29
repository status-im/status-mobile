(ns status-im.commands.utils
  (:require [clojure.set :as set]
            [clojure.walk :as w]
            [re-frame.core :refer [dispatch trim-v]]
            [status-im.ui.components.react :as components]
            [status-im.chat.views.input.validation-messages :as chat-validation-messages]
            [status-im.chat.views.api.choose-contact :as choose-contact]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.chat-preview :as chat-preview]
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
   :chat-preview-text    chat-preview/text
   :image                components/image
   :qr-code              qr-code-viewer/qr-code
   :linking              components/linking
   :slider               components/slider
   :scroll-view          components/scroll-view
   :web-view             components/web-view
   :touchable            components/touchable-highlight
   :activity-indicator   components/activity-indicator
   :validation-message   chat-validation-messages/validation-message
   :choose-contact       choose-contact/choose-contact-view
   :separator            parameter-box-separator})

(defn get-element [n]
  (elements (keyword (.toLowerCase n))))

(def events #{:onPress :onValueChange :onSlidingComplete})

(defn wrap-event [[_ event] bot-id] 
  #(dispatch [:suggestions-event! bot-id (update event 0 keyword) %]))

(defn check-events [m bot-id]
  (let [ks (set (keys m))
        evs (set/intersection ks events)]
    (reduce #(update %1 %2 wrap-event bot-id) m evs)))

(defn generate-hiccup
  ([markup]
   (generate-hiccup markup nil {}))
  ([markup bot-id bot-db] 
   (w/prewalk
     (fn [el]
       (cond

         (and (vector? el) (= "subscribe" (first el)))
         (let [path (mapv keyword (second el))]
           (get-in bot-db path))

         (and (vector? el) (string? (first el)))
         (-> el
             (update 0 get-element)
             (update 1 check-events bot-id))

         :else el))
     markup)))

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [trim-v middleware] handler)))
