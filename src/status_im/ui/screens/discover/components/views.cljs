(ns status-im.ui.screens.discover.components.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [status-im.components.react :as react]
            [status-im.ui.screens.discover.styles :as st]
            [status-im.components.status-view.view :as view]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.components.chat-icon.screen :as ci]
            [status-im.utils.platform :as platform]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.i18n :as i18n]))

(defn title [label-kw action-kw action-fn]
  [react/view st/title
   [react/text {:style      (get-in platform/platform-specific [:component-styles :discover :subtitle])
                :uppercase? (get-in platform/platform-specific [:discover :uppercase-subtitles?])
                :font       :medium}
    (i18n/label label-kw)]
   [react/touchable-highlight {:on-press action-fn}
    [react/view {} [react/text {:style st/title-action-text} (i18n/label action-kw)]]]])

(defn tags-menu [tags]
  [react/view st/tag-title-container
   (for [tag (take 3 tags)]
     ^{:key (str "tag-" tag)}
     [react/touchable-highlight {:on-press #(do (re-frame/dispatch [:set :discover-search-tags [tag]])
                                                (re-frame/dispatch [:navigate-to :discover-search-results]))}
      [react/view (merge (get-in platform/platform-specific [:component-styles :discover :tag])
                         {:margin-left 2 :margin-right 2})
       [react/text {:style st/tag-title
                    :font  :default}
        (str " #" tag)]]])])

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
           account-name       :name} current-account
          me?        (= account-address whisper-id)
          item-style (get-in platform/platform-specific [:component-styles :discover :item])]
      [react/view
       [react/view st/popular-list-item
        [view/status-view {:id     message-id
                           :style  (:status-text item-style)
                           :status status}]
        [react/view st/popular-list-item-second-row
         [react/view st/popular-list-item-name-container
          [react/view (merge st/popular-list-item-avatar-container
                             (:icon item-style))
           [ci/chat-icon
            (display-image me? account-photo-path contact-photo-path photo-path whisper-id)
            {:size 20}]]
          [react/text {:style           st/popular-list-item-name
                       :font            :medium
                       :number-of-lines 1}
           (display-name me? account-name contact-name name whisper-id)]]
         (when-not me?
           [react/touchable-highlight {:on-press #(re-frame/dispatch [:start-chat whisper-id])}
            [react/view st/popular-list-chat-action
             [vi/icon :icons/chats {:color "rgb(110, 0, 228)"}]
             [react/text {:style st/popular-list-chat-action-text} (i18n/label :t/chat)]]])]
        (when show-separator?
          [react/view st/separator])]])))
