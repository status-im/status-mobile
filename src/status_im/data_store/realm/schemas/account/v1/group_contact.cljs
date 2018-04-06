(ns status-im.data-store.realm.schemas.account.v1.group-contact
  (:require [taoensso.timbre :as log]))

(def schema {:name       :group-contact
             :properties {:identity   "string"}})
