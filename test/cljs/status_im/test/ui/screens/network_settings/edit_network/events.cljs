(ns status-im.test.ui.screens.network-settings.edit-network.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.network-settings.edit-network.events :as events]))

(deftest new-network
  (let [actual (events/new-network {:random-id "random-id"}
                                   "network-name"
                                   "upstream-url"
                                   :mainnet)]
    (is (= {:id     "randomid"
            :name   "network-name"
            :config {:NetworkId      1
                     :DataDir        "/ethereum/mainnet_rpc"
                     :UpstreamConfig {:Enabled true
                                      :URL "upstream-url"}}}
           actual))))
