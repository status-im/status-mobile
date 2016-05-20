(ns syng-im.new-group.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.utils.subs :as u]))

(register-sub :is-contact-selected?
  (u/contains-sub :selected-contacts))
