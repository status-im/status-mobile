(ns status-im.ui.screens.add-new.new-public-chat.view
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as common.styles]
            [status-im.ui.components.text-input.view :as text-input.view]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.add-new.new-public-chat.db :as db]
            [status-im.ui.screens.add-new.new-public-chat.styles :as styles]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            status-im.utils.db
            [status-im.utils.types :as types])
  (:require-macros
   [status-im.utils.slurp :refer [slurp]]
   [status-im.utils.views :as views]))

(defn- start-chat [topic]
  (re-frame/dispatch [:chat.ui/start-public-chat topic {:navigation-reset? true}])
  (re-frame/dispatch [:set :public-group-topic nil]))

(defn- chat-name-input [topic error]
  [react/view
   [react/view (merge add-new.styles/input-container {:margin-top 8})
    [react/text {:style styles/topic-hash} "#"]
    [react/view common.styles/flex
     [text-input.view/text-input-with-label
      {:container           styles/input-container
       :on-change-text      #(re-frame/dispatch [:set :public-group-topic %])
       :on-submit-editing   #(when (db/valid-topic? topic) (start-chat topic))
       :auto-capitalize     :none
       :auto-focus          false
       :accessibility-label :chat-name-input
       ;; Set default-value as otherwise it will
       ;; be erased in global `onWillBlur` handler
       :default-value       topic
       :placeholder         nil
       :return-key-type     :go
       :auto-correct        false}]]]
   (when error
     [tooltip/tooltip error styles/tooltip])])

(defn- public-chat-icon [topic]
  [react/view styles/public-chat-icon
   [react/text {:style styles/public-chat-icon-symbol}
    (first topic)]])

(defn- render-topic [topic]
  [react/touchable-highlight {:on-press            #(start-chat topic)
                              :accessibility-label :chat-item}
   [react/view
    [list/item
     [public-chat-icon topic]
     [list/item-primary-only
      topic]
     [list/item-icon {:icon      :main-icons/next
                      :icon-opts {:color :gray}}]]]])

(def default-public-chats-json
  (slurp "resources/default_public_chats.json"))

(def default-public-chats
  (memoize
   (fn []
     (types/json->clj (default-public-chats-json)))))

(views/defview new-public-chat []
  (views/letsubs [topic [:public-group-topic]
                  error [:public-chat.new/topic-error-message]]
    [react/keyboard-avoiding-view styles/group-container
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/public-chat)]
     [react/view styles/chat-name-container
      [react/text {:style styles/section-title}
       (i18n/label :t/public-group-topic)]
      [chat-name-input topic error]]
     [react/view styles/chat-name-container
      [react/text {:style styles/section-title}
       (i18n/label :t/selected)]]
     [list/flat-list {:data                         (default-public-chats)
                      :key-fn                       identity
                      :render-fn                    render-topic
                      :keyboard-should-persist-taps :always
                      :default-separator?           true}]]))
