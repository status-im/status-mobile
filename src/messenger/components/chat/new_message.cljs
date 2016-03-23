(ns messenger.components.chat.new-message
  (:require-macros
    [natal-shell.components :refer [view image text-input]])
  (:require [om.next :as om :refer-macros [defui]]
            [messenger.utils.resources :as res]
            [syng-im.utils.logging :as log]
            [messenger.utils.state :refer [from-state]]
            [messenger.comm.intercom :refer [send-msg]]))

(def local-state (atom {}))

(defui NewMessage
  static om/IQuery
  (query [this]
    '[:chat/chat-id])
  Object
  (render [this]
    (view {:style {:flexDirection   "row"
                   :margin          10
                   :height          40
                   :backgroundColor "#E5F5F6"
                   :borderRadius    5}}
          (image {:source res/mic
                  :style  {:marginTop  11
                           :marginLeft 14
                           :width      13
                           :height     20}})
          (text-input {:underlineColorAndroid "#9CBFC0"
                       :style                 {:flex       1
                                               :marginLeft 18
                                               :lineHeight 42
                                               :fontSize   14
                                               :fontFamily "Avenir-Roman"
                                               :color      "#9CBFC0"}
                       :autoFocus             true
                       :placeholder           "Enter your message here"
                       :value                 (from-state this :text)
                       :onChangeText          (fn [text]
                                                ;(log/debug (with-out-str (pr (js->clj (om/props this)))) (-> (om/props this) :chat-id))
                                                ;(om/set-state! this (clj->js {:text text}))
                                                (swap! local-state assoc :text text)
                                                )
                       :onSubmitEditing       (fn [e]
                                                (let [chat-id (-> (om/props this) :chat-id)
                                                      ;text    (from-state this :text)
                                                      text    (get @local-state :text)]
                                                  ;(om/set-state! this (clj->js {:text nil}))
                                                  (send-msg chat-id text)))})
          (image {:source res/smile
                  :style  {:marginTop   11
                           :marginRight 12
                           :width       18
                           :height      18}})
          (image {:source res/att
                  :style  {:marginTop   14
                           :marginRight 16
                           :width       17
                           :height      14}}))))

(def new-message (om/factory NewMessage))
