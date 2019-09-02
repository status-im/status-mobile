(ns status-im.ens.db
  (:require [cljs.spec.alpha :as spec]))

(spec/def ::state #{:too-short
                    :already-added
                    :searching
                    :invalid
                    :available
                    :taken
                    :owned
                    :connected
                    :connected-with-different-key})

(spec/def :ens/registration (spec/keys :opt-un [::state]))
