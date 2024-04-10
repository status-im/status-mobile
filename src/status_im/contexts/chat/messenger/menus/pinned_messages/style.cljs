(ns status-im.contexts.chat.messenger.menus.pinned-messages.style
  (:require
    [react-native.platform :as platform]))

(defn heading
  [community?]
  {:padding-horizontal 20
   :margin-bottom      (when-not community? 12)})

(def community-tag-container
  {:margin-horizontal 20
   :margin-top        4
   :margin-bottom     12})

(def no-pinned-messages-container
  {:justify-content :center
   :align-items     :center})

(def list-footer
  {:height (when platform/android? 12)})
