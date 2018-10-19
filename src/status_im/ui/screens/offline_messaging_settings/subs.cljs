(ns status-im.ui.screens.offline-messaging-settings.subs
  (:require [re-frame.core :as re-frame]
            status-im.ui.screens.offline-messaging-settings.edit-mailserver.subs
            [status-im.utils.ethereum.core :as ethereum]))

(re-frame/reg-sub :settings/current-wnode
                  (fn [db _]
                    (:inbox/current-id db)))

(re-frame/reg-sub :settings/fleet-wnodes
                  :<- [:settings/current-fleet]
                  :<- [:get :inbox/wnodes]
                  (fn [[current-fleet wnodes]]
                    (current-fleet wnodes)))
