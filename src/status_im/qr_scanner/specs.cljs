(ns status-im.qr-scanner.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :qr/qr-codes (s/nilable map?))                                     ;;on scan qr
(s/def :qr/qr-modal (s/nilable map?))                                     ;;used in qr modal screen
(s/def :qr/current-qr-context (s/nilable map?))