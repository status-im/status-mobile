(ns status-im.chat.screen
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.chat-icon.screen :as chat-icon-screen]
            [status-im.chat.styles.screen :as style]
            [status-im.utils.listview :as listview]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.invertible-scroll-view :as scroll-view]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.chat.views.toolbar-content :as toolbar-content]
            [status-im.chat.views.message.message :as message]
            [status-im.chat.views.message.datemark :as message-datemark]
            [status-im.chat.views.input.input :as input]
            [status-im.chat.views.actions :as actions]
            [status-im.chat.views.bottom-info :as bottom-info]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.sync-state.offline :as offline]
            [clojure.string :as string]))

(defview chat-icon []
  (letsubs [{:keys [chat-id group-chat name color]} [:get-current-chat]]
    [chat-icon-screen/chat-icon-view-action chat-id group-chat name color true]))

(defn- toolbar-action [show-actions?]
  [react/touchable-highlight
   {:on-press            #(re-frame/dispatch [:set-chat-ui-props {:show-actions? (not show-actions?)}])
    :accessibility-label :chat-menu}
   [react/view style/action
    (if show-actions?
      [vector-icons/icon :icons/dropdown-up]
      [chat-icon])]])

(defview add-contact-bar []
  (letsubs [chat-id          [:get-current-chat-id]
            pending-contact? [:current-contact :pending?]]
    (when pending-contact?
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:add-pending-contact chat-id])}
       [react/view style/add-contact
        [react/text {:style style/add-contact-text}
         (i18n/label :t/add-to-contacts)]]])))

(defview chat-toolbar []
  (letsubs [show-actions? [:get-current-chat-ui-prop :show-actions?]
            accounts      [:get-accounts]
            creating?     [:get :accounts/creating-account?]]
    [react/view
     [status-bar/status-bar]
     [toolbar/toolbar {:show-sync-bar? true}
      (when-not (or show-actions? creating?)
        (if (empty? accounts)
          [toolbar/nav-clear-text (i18n/label :t/recover)
           #(re-frame/dispatch [:navigate-to-modal :recover-modal])]
          toolbar/default-nav-back))
      [toolbar-content/toolbar-content-view]
      [toolbar-action show-actions?]]
     [add-contact-bar]]))

(defmulti message-row (fn [{{:keys [type]} :row}] type))

(defmethod message-row :datemark
  [{{:keys [value]} :row}]
  (react/list-item [message-datemark/chat-datemark value]))

(defmethod message-row :default
  [{:keys [group-chat current-public-key row]}]
  (react/list-item [message/chat-message (assoc row
                                                :group-chat group-chat
                                                :current-public-key current-public-key)]))

(defview messages-view [chat-id group-chat]
  (letsubs [messages           [:get-chat-messages chat-id]
            current-public-key [:get-current-public-key]]
    [react/list-view {:renderRow                 (fn [row _ index]
                                                   (message-row {:group-chat         group-chat
                                                                 :current-public-key current-public-key
                                                                 :row                row}))
                      :renderScrollComponent     #(scroll-view/invertible-scroll-view (js->clj %))
                      :onEndReached              #(re-frame/dispatch [:load-more-messages])
                      :enableEmptySections       true
                      :keyboardShouldPersistTaps (if platform/android? :always :handled)
                      :dataSource                (listview/to-datasource-inverted messages)}]))

(defview chat []
  (letsubs [{:keys [chat-id group-chat input-text]} [:get-current-chat]
            show-actions?                           [:get-current-chat-ui-prop :show-actions?]
            show-bottom-info?                       [:get-current-chat-ui-prop :show-bottom-info?]
            show-emoji?                             [:get-current-chat-ui-prop :show-emoji?]
            layout-height                           [:get :layout-height]]
    {:component-did-mount    #(re-frame/dispatch [:check-and-open-dapp!])
     :component-will-unmount #(re-frame/dispatch [:set-chat-ui-props {:show-emoji? false}])}
    [react/view {:style style/chat-view
                 :on-layout (fn [event]
                              (let [height (.. event -nativeEvent -layout -height)]
                                (when (not= height layout-height)
                                  (re-frame/dispatch [:set-layout-height height]))))}
     [chat-toolbar]
     [messages-view chat-id group-chat]
     [input/container {:text-empty? (string/blank? input-text)}]
     (when show-actions?
       [actions/actions-view])
     (when show-bottom-info?
       [bottom-info/bottom-info-view])
     [offline/offline-view {:top (get platform/platform-specific :status-bar-default-height)}]]))
