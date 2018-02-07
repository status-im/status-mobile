(ns status-im.ui.components.list-selection
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-sheet :as action-sheet]
            [status-im.ui.components.dialog :as dialog]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn- open-share [content]
  (when (or (:message content)
            (:url content))
    (.share react/sharing (clj->js content))))

(defn share-options [text]
  [{:label  (i18n/label :t/sharing-copy-to-clipboard)
    :action #(react/copy-to-clipboard text)}
   {:label  (i18n/label :t/sharing-share)
    :action #(open-share {:message text})}])

(defn show [options]
  (if platform/ios?
    (action-sheet/show options)
    (dialog/show options)))

(defn share [text dialog-title]
  (show {:title       dialog-title
         :options     (share-options text)
         :cancel-text (i18n/label :t/sharing-cancel)}))

(defn browse [link]
  (show {:title       (i18n/label :t/browsing-title)
         :options     [{:label  (i18n/label :t/browsing-open-in-browser)
                        :action #(re-frame/dispatch [:open-browser {:url link}])}
                       {:label  (i18n/label :t/browsing-open-in-web-browser)
                        :action #(.openURL react/linking link)}]
         :cancel-text (i18n/label :t/browsing-cancel)}))
