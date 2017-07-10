(ns status-im.qr-scanner.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :qr/qr-codes map?)                                     ;;on scan qr
(s/def :qr/qr-modal map?)                                     ;;used in qr modal screen
(s/def :qr/current-qr-context map?)