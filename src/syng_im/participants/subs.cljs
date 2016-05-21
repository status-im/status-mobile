(ns syng-im.participants.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.utils.subs :as u]))

(register-sub :is-participant-selected?
  (u/contains-sub :selected-participants))
