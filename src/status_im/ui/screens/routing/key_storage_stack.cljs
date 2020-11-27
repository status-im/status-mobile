(ns status-im.ui.screens.routing.key-storage-stack
  "Manage flow required to change key-storage location"
  (:require [status-im.ui.screens.routing.core :as navigation]
            [status-im.ui.screens.multiaccounts.key-storage.views :as key-storage.views]))

(defonce stack (navigation/create-stack))

(defn key-storage-stack []
  [stack {:initial-route-name :actions-not-logged-in
          :header-mode        :none}
   [{:name      :actions-not-logged-in
     :component key-storage.views/actions-not-logged-in}
    {:name      :actions-logged-in
     :component key-storage.views/actions-logged-in}
    {:name      :seed-phrase
     :component key-storage.views/seed-phrase}
    {:name      :storage
     :component key-storage.views/storage}]])
