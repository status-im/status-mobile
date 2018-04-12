(ns status-im.ui.screens.add-new.new-public-chat.view
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.add-new.new-public-chat.styles :as styles]
            [status-im.ui.screens.add-new.new-public-chat.db :as v]
            [status-im.ui.components.text-input.view :as text-input.view]
            status-im.utils.db
            [status-im.ui.components.styles :as common.styles]
            [cljs.spec.alpha :as spec]
            [status-im.ui.components.tooltip.views :as tooltip]))

(defn- chat-name-input [topic error]
  [react/view
   [react/view (merge add-new.styles/input-container {:margin-top 8})
    [react/text {:style styles/topic-hash} "#"]
    [react/view common.styles/flex
     [text-input.view/text-input-with-label
      {:container           styles/input-container
       :on-change-text      #(do
                               (re-frame/dispatch [:set :public-group-topic-error (when-not (spec/valid? ::v/topic %)
                                                                                    (i18n/label :t/topic-name-error))])
                               (re-frame/dispatch [:set :public-group-topic %]))
       :on-submit-editing   #(when (and topic (spec/valid? ::v/topic topic))
                               (re-frame/dispatch [:create-new-public-chat topic]))
       :auto-capitalize     :none
       :accessibility-label :chat-name-input
       :placeholder         nil}]]]
   (when error
     [tooltip/tooltip error styles/tooltip])])

(defn- public-chat-icon [topic]
  [react/view styles/public-chat-icon
   [react/text {:uppercase? true
                :style      styles/public-chat-icon-symbol}
    (first topic)]])

(defn- render-topic [topic]
  [react/touchable-highlight {:on-press            #(re-frame/dispatch [:create-new-public-chat topic])
                              :accessibility-label :chat-item}
   [react/view
    [list/item
     [public-chat-icon topic]
     [list/item-primary-only
      topic]
     [list/item-icon {:icon      :icons/forward
                      :icon-opts {:color :gray}}]]]])

(def default-public-chats ["status" "openbounty" "edcon" "ethereum"])

(views/defview new-public-chat []
  (views/letsubs [topic [:get :public-group-topic]
                  error [:get :public-group-topic-error]]
    [react/view styles/group-container
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/public-chat)]
     [react/view styles/chat-name-container
      [react/text {:style styles/section-title
                   :font  :medium}
       (i18n/label :t/public-group-topic)]
      [chat-name-input topic error]]
     [react/view styles/chat-name-container
      [react/text {:style styles/section-title
                   :font  :medium}
       (i18n/label :t/selected)]]
     [list/flat-list {:data               default-public-chats
                      :key-fn             identity
                      :render-fn          render-topic
                      :default-separator? true}]]))
