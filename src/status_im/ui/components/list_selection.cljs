(ns status-im.ui.components.list-selection
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-sheet :as action-sheet]
            [status-im.ui.components.dialog :as dialog]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.utils.http :as http]
            [status-im.ui.components.popup-menu.views :refer [show-desktop-menu]]))

(defn open-share [content]
  (when (or (:message content)
            (:url content))
    (.share react/sharing (clj->js content))))

(defn- message-options [message-id old-message-id text]
  [{:label  (i18n/label :t/message-reply)
    :action #(re-frame/dispatch [:chat.ui/reply-to-message message-id old-message-id])}
   {:label  (i18n/label :t/sharing-copy-to-clipboard)
    :action #(react/copy-to-clipboard text)}
   (when-not platform/desktop?
     {:label  (i18n/label :t/sharing-share)
      :action #(open-share {:message text})})])

(defn show [options]
  (cond
    platform/ios?     (action-sheet/show options)
    platform/android? (dialog/show options)
    platform/desktop? (show-desktop-menu (->> (:options options) (remove nil?)))))

(defn chat-message [message-id old-message-id text dialog-title]
  (show {:title       dialog-title
         :options     (message-options message-id old-message-id text)
         :cancel-text (i18n/label :t/message-options-cancel)}))

(defn- platform-web-browser []
  (if platform/ios? :t/browsing-open-in-ios-web-browser :t/browsing-open-in-android-web-browser))

(defn browse [link]
  (show {:title       (i18n/label :t/browsing-title)
         :options     [{:label  (i18n/label :t/browsing-open-in-status)
                        :action #(re-frame/dispatch [:browser.ui/open-in-status-option-selected link])}
                       {:label  (i18n/label (platform-web-browser))
                        :action #(.openURL (react/linking) (http/normalize-url link))}]
         :cancel-text (i18n/label :t/browsing-cancel)}))

(defn browse-in-web-browser [link]
  (show {:title       (i18n/label :t/browsing-title)
         :options     [{:label  (i18n/label (platform-web-browser))
                        :action #(.openURL (react/linking) (http/normalize-url link))}]
         :cancel-text (i18n/label :t/browsing-cancel)}))

(defn browse-dapp [link]
  (show {:title       (i18n/label :t/browsing-title)
         :options     [{:label  (i18n/label :t/browsing-open-in-status)
                        :action #(re-frame/dispatch [:browser.ui/open-in-status-option-selected link])}]
         :cancel-text (i18n/label :t/browsing-cancel)}))
