(ns status-im.protocol.protocol-handler
  (:require [status-im.utils.logging :as log]
            [status-im.constants :refer [ethereum-rpc-url]]
            [re-frame.core :refer [dispatch]]
            [status-im.models.protocol :refer [stored-identity]]
            [status-im.persistence.simple-kv-store :as kv]
            [status-im.models.chats :refer [active-group-chats]]))
