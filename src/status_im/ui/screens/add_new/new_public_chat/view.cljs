(ns status-im.ui.screens.add-new.new-public-chat.view
  (:require-macros [status-im.utils.views :as views])
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.text-field.view :as text-field]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.add-new.new-public-chat.styles :as styles]
            [status-im.ui.screens.add-new.new-public-chat.db :as v]
            status-im.utils.db))

(defn- topic-error-label [topic]
  (cond
    (not (spec/valid? :global/not-empty-string topic))
    (i18n/label :t/empty-topic)

    (not (spec/valid? ::v/topic topic))
    (i18n/label :t/topic-format)))

(defn- chat-name-input [topic]
  [react/view (merge add-new.styles/input-container {:margin-top 8})
   [react/text {:style styles/topic-hash} "#"]
   [text-field/text-field
    {:wrapper-style       styles/group-chat-name-wrapper
     :line-color          components.styles/color-transparent
     :focus-line-color    components.styles/color-transparent
     :label-hidden?       true
     :input-style         styles/group-chat-topic-input
     :on-change-text      #(re-frame/dispatch [:set :public-group-topic %])
     :on-submit-editing   #(when topic (re-frame/dispatch [:create-new-public-chat topic]))
     :value               topic
     :validator           #(re-matches #"[a-z\-]*" %)
     :auto-capitalize     :none
     :accessibility-label :chat-name-input}]])

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

(def default-public-chats ["status" "openbounty" "ethereum"])

(views/defview new-public-chat []
  (views/letsubs [topic [:get :public-group-topic]]
    [react/view styles/group-container
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/public-chat)]
     [react/view styles/chat-name-container
      [react/text {:style styles/section-title
                   :font  :medium}
       (i18n/label :t/public-group-topic)]
      [chat-name-input topic]]
     [react/view styles/chat-name-container
      [react/text {:style styles/section-title
                   :font  :medium}
       (i18n/label :t/selected)]]
     [list/flat-list {:data               default-public-chats
                      :render-fn          render-topic
                      :default-separator? true}]]))
