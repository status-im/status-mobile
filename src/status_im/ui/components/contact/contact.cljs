(ns status-im.ui.components.contact.contact
  (:require [status-im.ethereum.stateofus :as stateofus]
            [status-im.utils.gfycat.core :as gfycat]))

(defn format-name [{:keys [ens-verified name public-key]}]
  (if ens-verified
    (str "@" (or (stateofus/username name) name))
    (gfycat/generate-gfy public-key)))
