(ns status-im.ui.components.desktop.shortcuts
  (:require [status-im.react-native.js-dependencies :refer [desktop-shortcuts]]
            [status-im.ui.screens.desktop.main.tabs.home.views :as chat-list]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.utils :as utils]))

(defn register-shortcut [shortcut on-press]
  (.set desktop-shortcuts (clj->js {:shortcut shortcut
                                    :onPress on-press})))

(defn register-default-shortcuts []
  (.register desktop-shortcuts
             (clj->js (vector
                       {:shortcut "Ctrl+N"
                        :onPress #(re-frame/dispatch [:navigate-to :desktop/new-one-to-one])}
                       {:shortcut "Ctrl+G"
                        :onPress #(re-frame/dispatch [:navigate-to :desktop/new-group-chat])}
                       {:shortcut "Ctrl+P"
                        :onPress #(re-frame/dispatch [:navigate-to :desktop/new-public-chat])}
                       {:shortcut "Ctrl+F"
                        :onPress #(utils/show-popup "" "Ctrl+F")}))))

