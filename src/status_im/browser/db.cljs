(ns status-im.browser.db
  (:require [cljs.spec.alpha :as spec]))

(spec/def :browser/browser-id (spec/nilable string?))
(spec/def :browser/timestamp (spec/nilable int?))
(spec/def :browser/name (spec/nilable string?))
(spec/def :browser/url (spec/nilable string?))
(spec/def :browser/dapp? (spec/nilable boolean?))
(spec/def :browser/error? (spec/nilable boolean?))
(spec/def :browser/history (spec/nilable vector?))
(spec/def :browser/history-index (spec/nilable int?))
(spec/def :browser/unsafe? (spec/nilable boolean?))
(spec/def :browser/loading? (spec/nilable boolean?))
(spec/def :browser/resolving? (spec/nilable boolean?))
(spec/def :browser/url-editing? (spec/nilable boolean?))
(spec/def :browser/show-tooltip (spec/nilable keyword?))
(spec/def :browser/show-permission (spec/nilable map?))
(spec/def :browser/resolved-ens (spec/nilable map?))
(spec/def :browser/pending-permissions (spec/nilable list?))
(spec/def :browser/yielding-control? (spec/nilable boolean?))

(spec/def :browser/options
  (spec/nilable
   (spec/keys :opt-un [:browser/browser-id
                       :browser/url
                       :browser/loading?
                       :browser/resolving?
                       :browser/url-editing?
                       :browser/show-tooltip
                       :browser/show-permission
                       :browser/pending-permissions
                       :browser/yielding-control?
                       :browser/resolved-ens
                       :browser/error?])))

(spec/def :browser/browser
  (spec/keys :req-un [:browser/browser-id
                      :browser/timestamp]
             :opt-un [:browser/name
                      :browser/dapp?
                      :browser/history
                      :browser/history-index
                      :browser/unsafe?]))

(spec/def :browser/browsers (spec/nilable (spec/map-of :global/not-empty-string :browser/browser)))

(spec/def :dapp/dapp (spec/nilable string?))
(spec/def :dapp/permissions (spec/nilable vector?))

(spec/def :dapp/permission-map
  (spec/keys :req-un [:dapp/dapp]
             :opt-un [:dapp/permissions]))

(spec/def :dapps/permissions (spec/nilable (spec/map-of :global/not-empty-string :dapp/permission-map)))
