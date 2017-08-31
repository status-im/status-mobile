(ns status-im.chat.events
  (:require [re-frame.core :refer [trim-v]]
            [status-im.utils.handlers :refer [register-handler-db]]
            [status-im.chat.models :as model]
            status-im.chat.events.input
            status-im.chat.events.commands))

;;;; Handlers

(register-handler-db
  :set-layout-height
  [trim-v]
  (fn [db [height]]
    (assoc db :layout-height height)))

(register-handler-db
  :set-chat-ui-props
  [trim-v]
  (fn [db [kvs]]
    (model/set-chat-ui-props db kvs)))

(register-handler-db
  :toggle-chat-ui-props
  [trim-v]
  (fn [db [ui-element]]
    (model/toggle-chat-ui-prop db ui-element)))

(register-handler-db
  :show-message-details
  [trim-v]
  (fn [db [details]]
    (model/set-chat-ui-props db {:show-bottom-info? true
                                 :show-emoji?       false
                                 :bottom-info       details})))
