(ns status-im.ui.screens.network-settings.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(spec/def :networks/id string?)
(spec/def :networks/name string?)
(spec/def :networks/config map?)

(spec/def :networks/network
  (spec/keys :req-un [:networks/id :networks/name :networks/config]))

(spec/def :networks/selected-network :networks/network)

(spec/def :networks/networks (spec/nilable (spec/map-of :networks/id :networks/network)))

(spec/def :networks/manage (spec/nilable map?))
