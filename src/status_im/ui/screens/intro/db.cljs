(ns status-im.ui.screens.intro.db
  (:require [cljs.spec.alpha :as spec]
            status-im.multiaccounts.db))

(spec/def :intro-wizrad/encrypt-with-password? boolean?)
(spec/def :intro-wizard/multiaccounts
  (spec/coll-of :multiaccounts/generated-multiaccount))
(spec/def :intro-wizard/selected-storage-type? keyword?)
(spec/def :intro-wizard/selected-id :generated-multiaccounts/id)
(spec/def :intro-wizard/back-action keyword?)
(spec/def :intro-wizard/weak-password? boolean?)
(spec/def :intro-wizard/forward-action keyword?)
(spec/def :intro-wizard/first-time-setup? boolean?)
(spec/def :intro-wizard/step (spec/nilable keyword?))
(spec/def :intro-wizard/root-key :multiaccounts/generated-multiaccount)
(spec/def :intro-wizard/passphrase string?)
(spec/def :intro-wizard/recovering? boolean?)
(spec/def :intro-wizard/passphrase-word-count int?)
(spec/def :intro-wizard/derived :generated-multiaccounts/derived)
(spec/def :intro-wizard/next-button-disabled? boolean?)

(spec/def :intro-wizard/intro-wizard
  (spec/keys :req-un [:intro-wizrad/encrypt-with-password?
                      :intro-wizard/back-action
                      :intro-wizard/weak-password?
                      :intro-wizard/forward-action
                      :intro-wizard/first-time-setup?
                      :intro-wizard/step]
             :opt-un [:intro-wizard/selected-id
                      :intro-wizard/selected-storage-type?
                      :intro-wizard/multiaccounts
                      :intro-wizard/root-key
                      :intro-wizard/passphrase
                      :intro-wizard/passphrase-word-count
                      :intro-wizard/derived
                      :intro-wizard/next-button-disabled?]))
