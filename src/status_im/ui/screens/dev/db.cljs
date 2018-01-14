(ns status-im.ui.screens.dev.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(spec/def :dev/testfairy-enabled? boolean?)
(spec/def :dev/stub-status-go? boolean?)
(spec/def :dev/mainnet-networks-enabled? boolean?)
(spec/def :dev/erc20-enabled? boolean?)
(spec/def :dev/offline-inbox-enabled? boolean?)
(spec/def :dev/log-level #{"error"
                           "warn"
                           "info"
                           "debug"
                           "trace"})
(spec/def :dev/jsc-enabled? boolean?)
(spec/def :dev/queue-message-enabled? boolean?)

(spec/def :dev/settings
  (allowed-keys
    :opt-un [:dev/testfairy-enabled?
             :dev/stub-status-go?
             :dev/mainnet-networks-enabled?
             :dev/erc20-enabled?
             :dev/offline-inbox-enabled?
             :dev/log-level
             :dev/jsc-enabled?
             :dev/queue-message-enabled?]))
