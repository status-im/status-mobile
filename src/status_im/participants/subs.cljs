(ns status-im.participants.subs
  (:require [re-frame.core :refer [reg-sub]]
            [status-im.utils.subs :as u]))

(reg-sub :is-participant-selected?
  (u/contains-sub :selected-participants))
