(ns status-im.ui.components.list-selection
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.react :refer [copy-to-clipboard
                                                sharing
                                                linking]]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [status-im.i18n :refer [label]]))

(defn open-share [content]
  (when (or (:message content)
            (:url content))
    (.share sharing (clj->js content))))

(defn share-options [text]
  [{:text  (label :t/sharing-copy-to-clipboard)
    :value #(copy-to-clipboard text)}
   {:text  (label :t/sharing-share)
    :value #(open-share {:message text})}])

(defn share [text dialog-title]
  (let [list-selection-fn (:list-selection-fn platform-specific)]
    (list-selection-fn {:title       dialog-title
                        :options     (share-options text)
                        :callback    (fn [index]
                                       (case index
                                         0 (copy-to-clipboard text)
                                         1 (open-share {:message text})
                                         :default))
                        :cancel-text (label :t/sharing-cancel)})))

(defn browse [command link]
  (let [list-selection-fn (:list-selection-fn platform-specific)]
    (list-selection-fn {:title       (label :t/browsing-title)
                        :options     [{:text (label :t/browsing-browse)}
                                      {:text (label :t/browsing-open-in-web-browser)}]
                        :callback    (fn [index]
                                       (case index
                                         0 (do
                                             (dispatch [:select-chat-input-command
                                                        (assoc (first command) :prefill [link])
                                                        nil
                                                        true])
                                             (js/setTimeout #(dispatch [:send-current-message]) 100))
                                         1 (.openURL linking link)
                                         :default))
                        :cancel-text (label :t/browsing-cancel)})))

(defn share-or-open-map [address lat lng]
  (let [list-selection-fn (:list-selection-fn platform-specific)]
    (list-selection-fn {:title       (label :t/location)
                        :options     [{:text  (label :t/sharing-copy-to-clipboard-address)}
                                      {:text  (label :t/sharing-copy-to-clipboard-coordinates)}
                                      {:text  (label :t/open-map)}]
                        :callback    (fn [index]
                                       (case index
                                         0 (copy-to-clipboard address)
                                         1 (copy-to-clipboard (str lng "," lat))
                                         2 (.openURL linking (if ios?
                                                               (str "http://maps.apple.com/?ll=" lng "," lat)
                                                               (str "geo:" lng "," lat)))
                                         :default))
                        :cancel-text (label :t/cancel)})))
