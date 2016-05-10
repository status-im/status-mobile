(ns syng-im.navigation.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub :view-id
  (fn [db _]
    (reaction (@db :view-id))))

(register-sub :navigation-stack
  (fn [db _]
    (reaction (:navigation-stack @db))))
