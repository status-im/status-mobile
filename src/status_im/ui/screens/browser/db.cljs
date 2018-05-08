(ns status-im.ui.screens.browser.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(spec/def :browser/browser-id (spec/nilable string?))
(spec/def :browser/contact (spec/nilable string?))
(spec/def :browser/timestamp (spec/nilable int?))
(spec/def :browser/url (spec/nilable string?))
(spec/def :browser/photo-path (spec/nilable string?))
(spec/def :browser/name (spec/nilable string?))
(spec/def :browser/dapp? (spec/nilable boolean?))
(spec/def :browser/fullscreen? (spec/nilable boolean?))
(spec/def :browser/can-go-back? (spec/nilable boolean?))
(spec/def :browser/can-go-forward? (spec/nilable boolean?))

(spec/def :browser/options
  (allowed-keys
   :opt-un [:browser/browser-id
            :browser/can-go-back?
            :browser/can-go-forward?
            :browser/fullscreen?]))

(spec/def :browser/browser
  (allowed-keys
   :req-un [:browser/browser-id
            :browser/timestamp]
   :opt-un [:browser/name
            :browser/dapp?
            :browser/url
            :browser/contact]))

(spec/def :browser/browsers (spec/nilable (spec/map-of :global/not-empty-string :browser/browser)))