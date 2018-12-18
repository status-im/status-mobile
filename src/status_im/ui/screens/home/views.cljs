(ns status-im.ui.screens.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.utils.datetime :as time]
            [status-im.ui.components.react :as components]
            [status-im.utils.utils :as utils]))

(defn- toolbar [show-welcome? show-sync-state sync-state latest-block-number]
  (when-not (and show-welcome?
                 platform/android?)
    [toolbar/toolbar nil nil
     (when-not show-welcome?
       (if show-sync-state
         [react/view {:style styles/sync-wrapper}
          [components.common/logo styles/toolbar-logo]

          [react/touchable-highlight {:accessibility-label :new-chat-button
                                      :on-press            #(re-frame/dispatch [:home.ui/sync-info-pressed])}
           [react/text {:style styles/sync-info}
            (str "LES: 'latest' #" latest-block-number "\n"
                 (if sync-state
                   (str "syncing " (:currentBlock sync-state) " of " (:highestBlock sync-state) " blocks...")
                   (str "not syncing")))]]]
         [toolbar/content-wrapper
          [components.common/logo styles/toolbar-logo]]))
     [toolbar/actions
      (when platform/ios?
        [(-> (toolbar.actions/add true #(re-frame/dispatch [:navigate-to :new]))
             (assoc-in [:icon-opts :accessibility-label] :new-chat-button))])]]))

(defn- home-action-button []
  [react/view styles/action-button-container
   [react/touchable-highlight {:accessibility-label :new-chat-button
                               :on-press            #(re-frame/dispatch [:navigate-to :new])}
    [react/view styles/action-button
     [icons/icon :icons/add {:color :white}]]]])

(defn home-list-item [[home-item-id home-item]]
  (let [delete-action   (if (:chat-id home-item)
                          (if (and (:group-chat home-item)
                                   (not (:public? home-item)))
                            :group-chats.ui/remove-chat-pressed
                            :chat.ui/remove-chat)
                          :browser.ui/remove-browser-pressed)
        inner-item-view (if (:chat-id home-item)
                          inner-item/home-list-chat-item-inner-view
                          inner-item/home-list-browser-item-inner-view)]
    [list/deletable-list-item {:type      :chats
                               :id        home-item-id
                               :on-delete #(do
                                             (re-frame/dispatch [:set-swipe-position :chats home-item-id false])
                                             (re-frame/dispatch [delete-action home-item-id]))}
     [inner-item-view home-item]]))

;;do not remove view-id and will-update or will-unmount handlers, this is how it works
(views/defview welcome [view-id]
  (views/letsubs [handler #(re-frame/dispatch [:set-in [:accounts/create :show-welcome?] false])]
    {:component-will-update  handler
     :component-will-unmount handler}
    [react/view {:style styles/welcome-view}
     [react/view {:style styles/welcome-image-container}
      [react/image {:source (:welcome-image resources/ui)
                    :style  styles/welcome-image}]]
     [react/i18n-text {:style styles/welcome-text :key :welcome-to-status}]
     [react/view
      [react/i18n-text {:style styles/welcome-text-description
                        :key   :welcome-to-status-description}]]]))

(views/defview chats-list []
  (views/letsubs [home-items [:home-items]]
    (if (empty? home-items)
      [react/view styles/no-chats
       [react/i18n-text {:style styles/no-chats-text :key :no-recent-chats}]]
      [list/flat-list {:data      home-items
                       :key-fn    first
                       :render-fn (fn [home-item]
                                    [home-list-item home-item])}])))

(views/defview home [loading?]
  (views/letsubs [show-welcome? [:get-in [:accounts/create :show-welcome?]]
                  view-id [:get :view-id]
                  sync-state [:chain-sync-state]
                  latest-block-number [:latest-block-number]
                  rpc-network? [:current-network-uses-rpc?]
                  network-initialized? [:current-network-initialized?]]
    {:component-did-mount
     (fn [this]
       (let [[_ loading?] (.. this -props -argv)]
         (when loading?
           (utils/set-timeout
            #(re-frame/dispatch [:init-chats])
            100))))}
    [react/view styles/container
     [toolbar show-welcome? (and network-initialized? (not rpc-network?)) sync-state latest-block-number]
     (cond show-welcome?
           [welcome view-id]
           loading?
           [react/view {:style {:flex            1
                                :justify-content :center
                                :align-items     :center}}
            [connectivity/connectivity-view]
            [components/activity-indicator {:flex 1
                                            :animating true}]]
           :else
           [react/view
            [connectivity/connectivity-view]
            [chats-list]])
     (when platform/android?
       [home-action-button])]))

(views/defview home-wrapper []
  (views/letsubs [loading? [:get :chats/loading?]]
    [home loading?]))
