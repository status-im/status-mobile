(ns status-im.components.list-selection
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [copy-to-clipboard
                                                linking]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.i18n :refer [label]]))

(def class (js/require "react-native-share"))

(defn open [opts]
  (.open class (clj->js opts)))

(defn share-options [text]
  [{:text (label :t/sharing-copy-to-clipboard) :value #(copy-to-clipboard text)}
   {:text (label :t/sharing-share)             :value #(open {:message text})}])

(defn share [text dialog-title]
  (let [list-selection-fn (:list-selection-fn platform-specific)]
    (list-selection-fn {:title       dialog-title
                        :options     (share-options text)
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
                                             (dispatch [:select-chat-input-command {:name    "browse"
                                                                                    :prefill [link]}])
                                             (js/setTimeout #(dispatch [:send-current-message]) 500))
                                         1 (.openURL linking link)
                                         :default))
                        :cancel-text (label :t/browsing-cancel)})))

