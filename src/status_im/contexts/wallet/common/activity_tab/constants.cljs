(ns status-im.contexts.wallet.common.activity-tab.constants)

(def ^:const wallet-activity-type-send 0)
(def ^:const wallet-activity-type-receive 1)
(def ^:const wallet-activity-type-swap 3)
(def ^:const wallet-activity-type-bridge 4)
(def ^:const wallet-activity-type-approval 7)

(def ^:const wallet-activity-status-failed 0)
(def ^:const wallet-activity-status-pending 1)
(def ^:const wallet-activity-status-confirmed 2)
(def ^:const wallet-activity-status-finalised 3)

(def ^:const wallet-activity-token-type-erc-721 2)
(def ^:const wallet-activity-token-type-erc-1155 3)

(def ^:const wallet-activity-status->name
  {wallet-activity-status-failed    :failed
   wallet-activity-status-pending   :pending
   wallet-activity-status-confirmed :confirmed
   wallet-activity-status-finalised :finalised})
