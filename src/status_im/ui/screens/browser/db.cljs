(ns status-im.ui.screens.browser.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(spec/def :browser/browser-id (spec/nilable string?))
(spec/def :browser/timestamp (spec/nilable int?))
(spec/def :browser/name (spec/nilable string?))
(spec/def :browser/dapp? (spec/nilable boolean?))
(spec/def :browser/error? (spec/nilable boolean?))
(spec/def :browser/history (spec/nilable vector?))
(spec/def :browser/history-index (spec/nilable int?))

(spec/def :browser/options
  (allowed-keys
   :opt-un [:browser/browser-id
            :browser/error?]))

(spec/def :browser/browser
  (allowed-keys
   :req-un [:browser/browser-id
            :browser/timestamp]
   :opt-un [:browser/name
            :browser/dapp?
            :browser/history
            :browser/history-index]))

(spec/def :browser/browsers (spec/nilable (spec/map-of :global/not-empty-string :browser/browser)))

(spec/def :dapp/dapp (spec/nilable string?))
(spec/def :dapp/permissions (spec/nilable vector?))

(spec/def :dapp/permission-map
  (allowed-keys
   :req-un [:dapp/dapp]
   :opt-un [:dapp/permissions]))

(spec/def :dapps/permissions (spec/nilable (spec/map-of :global/not-empty-string :dapp/permission-map)))