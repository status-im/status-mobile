(ns status-im.ui.screens.qr-scanner.db
  (:require [cljs.spec.alpha :as s]))

;;on scan qr
(s/def :qr/qr-codes (s/nilable map?))
;;used in qr modal screen
(s/def :qr/qr-modal (s/nilable map?))
(s/def :qr/current-qr-context (s/nilable map?))