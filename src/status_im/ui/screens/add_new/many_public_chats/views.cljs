(ns status-im.ui.screens.add-new.many-public-chats.views
  (:require-macros
   [status-im.utils.views :as views]
   [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.components.text-input.view :as text-input.view]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.add-new.new-public-chat.db :as db]
            [status-im.ui.components.styles :as common.styles]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(def input-container
  {:padding          0
   :padding-right    16
   :background-color nil})

(def input-container-2
  {:padding-right 16
   :padding-left  16})

(def tooltip
  {:bottom-value -15
   :color        colors/red-light
   :font-size    12})

(def group-chat-name-input
  {:font-size      17
   :padding-bottom 0
   :letter-spacing -0.2
   :color          colors/black})

(def topic-hash
  (merge group-chat-name-input
         {:margin-left 14}))

(defn- chat-name-input [error]
  [react/view
   [react/view (merge add-new.styles/input-container {:margin-top 8})
    [react/text {:style topic-hash} "#"]
    [react/view common.styles/flex
     [text-input.view/text-input-with-label
      {:container           input-container
       :on-change-text      #(re-frame/dispatch [:set :public-chats/base-topic %])
       :auto-capitalize     :none
       :auto-focus          false
       :accessibility-label :base-topic-input
       :placeholder         nil
       :return-key-type     :go
       :auto-correct        false}]]]
   (when error
     [tooltip/tooltip error tooltip])])

(defn- base-message-input []
  [react/view (merge add-new.styles/input-container {:margin-top 8})
   [react/view common.styles/flex
    [text-input.view/text-input-with-label
     {:container           input-container-2
      :on-change-text      #(re-frame/dispatch [:set :public-chats/base-message %])
      :auto-capitalize     :none
      :auto-focus          false
      :accessibility-label :base-topic-input
      :placeholder         nil
      :return-key-type     :go
      :auto-correct        false}]]])

(defn- chats-num-input []
  [react/view (merge add-new.styles/input-container {:margin-top 8})
   [react/view common.styles/flex
    [text-input.view/text-input-with-label
     {:container           input-container-2
      :on-change-text      #(re-frame/dispatch [:set :public-chats/total-num %])
      :auto-capitalize     :none
      :auto-focus          false
      :accessibility-label :chats-total-num-input
      :placeholder         nil
      :return-key-type     :go
      :auto-correct        false}]]])

(defn- messages-num-input []
  [react/view (merge add-new.styles/input-container {:margin-top 8})
   [react/view common.styles/flex
    [text-input.view/text-input-with-label
     {:container           input-container-2
      :on-change-text      #(re-frame/dispatch [:set :public-chats/messages-num %])
      :auto-capitalize     :none
      :auto-focus          false
      :accessibility-label :chats-total-num-input
      :placeholder         nil
      :return-key-type     :go
      :auto-correct        false}]]])

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color colors/white})

(def chat-name-container
  {:margin-top 10})

(defstyle members-text
  {:color   colors/gray
   :ios     {:letter-spacing -0.2
             :font-size      16}
   :android {:font-size 14}})

(def section-title
  (merge members-text
         {:padding-horizontal 16}))

(views/defview many-public-chats []
  (views/letsubs [topic ""
                  error [:public-chat.new/topic-error-message]]
    [react/keyboard-avoiding-view group-container
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      "Many many pub chats"]
     [react/view chat-name-container
      [react/text {:style section-title
                   :font  :medium}
       "Base topic"]
      [chat-name-input error]]
     [react/view chat-name-container
      [react/text {:style section-title
                   :font  :medium}
       "total number of chats"]
      [chats-num-input]]
     [react/view chat-name-container
      [react/text {:style section-title
                   :font  :medium}
       "base message"]
      [base-message-input]]
     [react/view chat-name-container
      [react/text {:style section-title
                   :font  :medium}
       "total number of messages"]
      [messages-num-input]]
     [react/text
      {:on-press #(re-frame/dispatch [:add-many-pub-chats])
       :style {:margin-top 20
               :margin-left 20
               :font-size 20}}
      "CREATE"]]))
