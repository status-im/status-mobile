(ns status-im.components.list-selection
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [copy-to-clipboard
                                                linking]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.i18n :refer [label]]))

(def class (js/require "react-native-share"))

(defn open [opts]
  (.open class (clj->js opts)))

(defn share [text dialog-title]
  (let [list-selection-fn (:list-selection-fn platform-specific)]
    (list-selection-fn {:title       dialog-title
                        :options     [(label :t/sharing-copy-to-clipboard) (label :t/sharing-share)]
                        :callback    (fn [index]
                                       (case index
                                         0 (copy-to-clipboard text)
                                         1 (open {:message text})
                                         :default))
                        :cancel-text (label :t/sharing-cancel)})))

(defn browse [link]
  (let [list-selection-fn (:list-selection-fn platform-specific)]
    (list-selection-fn {:title       (label :t/browsing-title)
                        :options     [(label :t/browsing-browse) (label :t/browsing-open-in-web-browser)]
                        :callback    (fn [index]
                                       (case index
                                         0 (do
                                             (dispatch [:set-chat-command :browse])
                                             (dispatch [:fill-chat-command-content link])
                                             (js/setTimeout #(dispatch [:send-command!]) 500))
                                         1 (.openURL linking link)
                                         :default))
                        :cancel-text (label :t/browsing-cancel)})))

