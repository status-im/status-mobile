(ns status-im.contexts.wallet.router.schema)

(def ?fee-modes
  [:enum
   :tx-fee-mode/custom
   :tx-fee-mode/normal
   :tx-fee-mode/fast
   :tx-fee-mode/urgent])

(def ^:private ?hex
  [:and :string [:re #"^0x[0-9a-fA-F]+$"]])

(def ^:private ?suggested-levels-for-max-fees-per-gas
  [:map {:closed? true}
   [:low ?hex]
   [:medium ?hex]
   [:high ?hex]
   [:low-estimated-time :int]
   [:medium-estimated-time :int]
   [:high-estimated-time :int]
   [:low-priority ?hex]
   [:medium-priority ?hex]
   [:high-priority ?hex]])

(def ^:private ?chain
  [:map
   [:short-name :string]
   [:chain-id :int]
   [:related-chain-id :int]
   [:chain-name :string]
   [:native-currency-name :string]])

(def ?route
  [:map
   ;; Route/Tx info
   [:processor-name :string]
   [:router-input-params-uuid :string]
   [:tx-packed-data :string]
   [:tx-nonce ?hex]
   [:suggested-tx-nonce ?hex]
   [:used-contract-address ?hex]
   [:from-chain ?chain]
   [:to-chain ?chain]

   ;; Amounts
   [:amount-in ?hex]
   [:amount-out ?hex]
   [:amount-in-locked :boolean]
   [:required-native-balance :int]
   [:required-token-balance :int]

   ;; Fees
   [:tx-gas-amount :int]
   [:tx-token-fees ?hex]
   [:tx-total-fee ?hex]
   [:tx-priority-fee ?hex]
   [:tx-base-fee ?hex]
   [:tx-fee ?hex]
   [:tx-l-1-fee ?hex]
   [:tx-bonder-fees [:maybe ?hex]]
   [:tx-gas-fee-mode :int]
   [:tx-estimated-time :int]
   [:tx-max-fees-per-gas ?hex]
   [:suggested-min-priority-fee ?hex]
   [:suggested-max-priority-fee ?hex]
   [:suggested-tx-gas-amount :int]
   [:suggested-approval-gas-amount :int]
   [:suggested-levels-for-max-fees-per-gas ?suggested-levels-for-max-fees-per-gas]
   [:current-base-fee ?hex]
   [:subtract-fees :boolean]

   ;; Approval
   [:approval-required :boolean]
   [:approval-tx-nonce [:maybe ?hex]]
   [:suggested-approval-tx-nonce [:maybe ?hex]]
   [:approval-contract-address [:maybe ?hex]]
   [:approval-packed-data [:maybe :string]]
   [:approval-amount-required [:maybe ?hex]]
   [:approval-fee [:maybe ?hex]]
   [:approval-l-1-fee [:maybe ?hex]]
   [:approval-gas-amount [:maybe :int]]
   [:approval-priority-fee [:maybe ?hex]]
   [:approval-base-fee [:maybe ?hex]]
   [:approval-max-fees-per-gas [:maybe ?hex]]
   [:approval-estimated-time [:maybe :int]]
   [:approval-gas-fee-mode [:maybe :int]]])
