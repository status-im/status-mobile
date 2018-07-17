(ns status-im.ui.screens.home.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.utils :as utils]
            [status-im.models.browser :as browser]))

(re-frame/reg-sub :home-items
                  :<- [:get-active-chats]
                  :<- [:browsers]
                  (fn [[chats browsers]]
                    ;; Status react issue #3604:
                    ;; de-dupe visits to same URL on home/chat list
                    (sort-by #(-> % second :timestamp) >
                             (merge chats
                                    (utils/distinct-by-group browsers browser/get-current-url :timestamp)))))
