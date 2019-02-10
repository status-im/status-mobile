(ns status-im.ui.screens.bootnodes-settings.db
  (:require
   [clojure.string :as string]
   [cljs.spec.alpha :as spec]))

(spec/def ::not-blank-string (complement string/blank?))

(spec/def :bootnode/address ::not-blank-string)
(spec/def :bootnode/name ::not-blank-string)
(spec/def :bootnode/id ::not-blank-string)
(spec/def :bootnode/chain ::not-blank-string)
(spec/def :bootnode/bootnode (spec/keys :req-un [:bootnode/chain
                                                 :bootnode/address
                                                 :bootnode/name
                                                 :bootnode/id]))

(spec/def :bootnodes/bootnodes (spec/nilable (spec/map-of :bootnode/id (spec/map-of :bootnode/id :bootnode/bootnode))))
