(ns status-im.ui.screens.add-new.db
  (:require [cljs.spec.alpha :as spec]
            status-im.contact.db))

(spec/def :new/open-dapp (spec/nilable :contact/contact))
