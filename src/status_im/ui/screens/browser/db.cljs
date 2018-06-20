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
(spec/def :browser/error? (spec/nilable boolean?))
(spec/def :browser/history (spec/nilable vector?))
(spec/def :browser/history-index (spec/nilable int?))
(spec/def :browser/dont-store-history-on-nav-change? (spec/nilable boolean?))

(spec/def :browser/options
  (allowed-keys
   :opt-un [:browser/browser-id
            :browser/fullscreen?
            :browser/error?
            :browser/dont-store-history-on-nav-change?]))

(spec/def :browser/browser
  (allowed-keys
   :req-un [:browser/browser-id
            :browser/timestamp]
   :opt-un [:browser/name
            :browser/dapp?
            :browser/url
            :browser/contact
            :browser/history
            :browser/history-index]))

(spec/def :browser/browsers (spec/nilable (spec/map-of :global/not-empty-string :browser/browser)))