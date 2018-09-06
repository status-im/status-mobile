(ns status-im.ui.screens.fleet-settings.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.config :as config]
            [status-im.fleet.core :as fleet]))

(re-frame/reg-sub
 :settings/current-fleet
 (fn [db _]
   (fleet/current-fleet db)))
