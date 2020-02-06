(ns status-im.ui.screens.add-new.new-chat.db
  (:require [status-im.ethereum.ens :as ens]
            [cljs.spec.alpha :as spec]))

(defn own-public-key?
  [{:keys [multiaccount]} public-key]
  (= (:public-key multiaccount) public-key))

(defn validate-pub-key [db public-key]
  (cond
    (or (not (spec/valid? :global/public-key public-key))
        (= public-key ens/default-key))
    :invalid
    (own-public-key? db public-key)
    :yourself))