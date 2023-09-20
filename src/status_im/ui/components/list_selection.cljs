(ns status-im.ui.components.list-selection
  (:require [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.ui.components.action-sheet :as action-sheet]
            [status-im.ui.components.dialog :as dialog]
            [status-im.ui.components.react :as react]
            [utils.url :as url]
            [react-native.platform :as platform]))

(defn open-share
  [content]
  (when (or (:message content)
            (:url content))
    (.share ^js react/sharing (clj->js content))))

(defn show
  [options]
  (cond
    platform/ios?     (action-sheet/show options)
    platform/android? (dialog/show options)))

(defn- platform-web-browser
  []
  (if platform/ios? :t/browsing-open-in-ios-web-browser :t/browsing-open-in-android-web-browser))

(defn browse
  [link]
  (show {:title       (i18n/label :t/browsing-title)
         :options     [{:label  (i18n/label :t/browsing-open-in-status)
                        :action #(re-frame/dispatch [:browser.ui/open-url link])}
                       {:label  (i18n/label (platform-web-browser))
                        :action #(.openURL ^js react/linking (url/normalize-url link))}]
         :cancel-text (i18n/label :t/browsing-cancel)}))

(defn browse-in-web-browser
  [link]
  (show {:title       (i18n/label :t/browsing-title)
         :options     [{:label  (i18n/label (platform-web-browser))
                        :action #(.openURL ^js react/linking (url/normalize-url link))}]
         :cancel-text (i18n/label :t/browsing-cancel)}))
