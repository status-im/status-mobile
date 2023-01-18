(ns status-im.ui.screens.add-new.new-public-chat.view
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.add-new.db :as db]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [i18n.i18n :as i18n])
  (:require-macros [status-im.utils.views :as views]))

(defn- start-chat
  [topic]
  (re-frame/dispatch [:chat.ui/start-public-chat topic])
  (re-frame/dispatch [:set :public-group-topic nil]))

(defn- hash-icon
  []
  [icons/icon :main-icons/channel {:color colors/gray}])

(defn- chat-name-input
  [topic error]
  [quo/text-input
   {:on-change-text      #(re-frame/dispatch [:set :public-group-topic %])
    :on-submit-editing   #(when (db/valid-topic? topic) (start-chat topic))
    :auto-capitalize     :none
    :auto-focus          false
    :accessibility-label :chat-name-input
    :before              {:component [hash-icon]}
    ;; Set default-value as otherwise it will
    ;; be erased in global `onWillBlur` handler
    :default-value       topic
    :placeholder         "chat-name"
    :return-key-type     :go
    :auto-correct        false
    :error               error}])

(views/defview new-public-chat
  []
  (views/letsubs [topic [:public-group-topic]
                  error [:public-chat.new/topic-error-message]]
    [react/scroll-view {:style {:flex 1}}
     [react/view {:padding-horizontal 16}
      [react/view {:align-items :center :padding-vertical 8}
       [react/image
        {:source (:new-chat-header resources/ui)
         :style  {:width 160 :height 160}}]]
      [react/text {:style {:text-align :center :margin-bottom 16 :line-height 22}}
       (i18n/label :t/public-chat-description)]
      [chat-name-input topic error]]]))
