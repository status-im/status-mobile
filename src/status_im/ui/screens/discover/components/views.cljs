(ns status-im.ui.screens.discover.components.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [status-im.components.react :as react]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.components.status-view.view :as view]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.components.chat-icon.screen :as chat-icon]
            [status-im.components.icons.vector-icons :as vector-icons]
            [status-im.i18n :as i18n]))

(defn title [label-kw action-kw action-fn active?]
  [react/view styles/title
   [react/text {:style      styles/title-text
                :font       :medium}
    (i18n/label label-kw)]
   [react/touchable-highlight {:on-press action-fn}
    [react/view {}
     ;; NOTE(oskarth): text-transform to uppercase not supported as RN style
     ;; https://github.com/facebook/react-native/issues/2088
     [react/text {:style      (styles/title-action-text active?)
                  :uppercase? (:uppercase? styles/subtitle-text-augment)}
      (i18n/label action-kw)]]]])

(defn display-name [me? account-name contact-name name whisper-id]
  (cond
    me? account-name                                        ;status by current user
    (not (str/blank? contact-name)) contact-name            ; what's the
    (not (str/blank? name)) name                            ;difference
    :else (gfycat/generate-gfy whisper-id)))

(defn display-image [me? account-photo-path contact-photo-path photo-path whisper-id]
  (cond
    me? account-photo-path
    (not (str/blank? contact-photo-path)) contact-photo-path
    (not (str/blank? photo-path)) photo-path
    :else (identicon/identicon whisper-id)))

(defview discover-list-item [{:keys [message show-separator? current-account]}]
  (letsubs [{contact-name       :name
             contact-photo-path :photo-path} [:get-in [:contacts/contacts (:whisper-id message)]]]
    (let [{:keys [name photo-path whisper-id message-id status]} message
          {account-photo-path :photo-path
           account-address    :public-key
           account-name       :name}                             current-account
          me?                                                    (= account-address whisper-id)]
      [react/view
       [react/view styles/discover-list-item
        [view/status-view {:id     message-id
                           :style  styles/discover-item-status-text
                           :status status}]
        [react/view styles/discover-list-item-second-row
         [react/view styles/discover-list-item-name-container
          [react/view styles/discover-list-item-avatar-container
           [chat-icon/chat-icon
            (display-image me? account-photo-path contact-photo-path photo-path whisper-id)
            {:size 20}]]
          [react/text {:style           styles/discover-list-item-name
                       :font            :medium
                       :number-of-lines 1}
           (display-name me? account-name contact-name name whisper-id)]]

         (when-not me?
           [react/touchable-highlight {:on-press #(re-frame/dispatch [:start-chat whisper-id])}
            [react/view styles/popular-list-chat-action
             [vector-icons/icon :icons/chats {:color "rgb(110, 0, 228)"}]
             [react/text {:style styles/popular-list-chat-action-text} (i18n/label :t/chat)]]])]
        (when show-separator?
          [react/view styles/separator])]])))
