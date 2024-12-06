(ns status-im.contexts.wallet.common.activity-tab.constants)

(def ^:const wallet-activity-error-code-success 1)
(def ^:const wallet-activity-error-code-task-canceled 2)
(def ^:const wallet-activity-error-code-failed 3)

(def ^:const wallet-activity-type-send 0)
(def ^:const wallet-activity-type-receive 1)
(def ^:const wallet-activity-type-buy 2)
(def ^:const wallet-activity-type-swap 3)
(def ^:const wallet-activity-type-bridge 4)
(def ^:const wallet-activity-type-contract-deployment 5)
(def ^:const wallet-activity-type-mint 6)
(def ^:const wallet-activity-type-approval 7)

(def ^:const wallet-activity-status-failed 0)
(def ^:const wallet-activity-status-pending 1)
(def ^:const wallet-activity-status-confirmed 2)
(def ^:const wallet-activity-status-finalised 3)

(def ^:const wallet-activity-token-type-native 0)
(def ^:const wallet-activity-token-type-erc-20 1)
(def ^:const wallet-activity-token-type-erc-721 2)
(def ^:const wallet-activity-token-type-erc-1155 3)

(def ^:const wallet-activity-id->name
  {wallet-activity-type-send                :send
   wallet-activity-type-receive             :receive
   wallet-activity-type-buy                 :buy
   wallet-activity-type-swap                :swap
   wallet-activity-type-bridge              :bridge
   wallet-activity-type-contract-deployment :contract-deployment
   wallet-activity-type-mint                :mint})

(def ^:const wallet-activity-status->name
  {wallet-activity-status-failed    :failed
   wallet-activity-status-pending   :pending
   wallet-activity-status-confirmed :confirmed
   wallet-activity-status-finalised :finalised})

(def ^:const second-tag-prefix
  {wallet-activity-type-send                :t/from
   wallet-activity-type-receive             :t/from
   wallet-activity-type-buy                 :t/on
   wallet-activity-type-swap                :t/to
   wallet-activity-type-bridge              :t/from
   wallet-activity-type-contract-deployment :t/via
   wallet-activity-type-mint                :t/at})

(def ^:const third-tag-prefix
  {wallet-activity-type-send                :t/to
   wallet-activity-type-receive             :t/to
   wallet-activity-type-buy                 :t/to
   wallet-activity-type-swap                :t/on
   wallet-activity-type-bridge              :t/to
   wallet-activity-type-contract-deployment :t/on
   wallet-activity-type-mint                :t/via})

(def ^:const fourth-tag-prefix
  {wallet-activity-type-send    :t/via
   wallet-activity-type-receive :t/via
   wallet-activity-type-buy     :t/via
   wallet-activity-type-swap    :t/via
   wallet-activity-type-bridge  :t/in})
