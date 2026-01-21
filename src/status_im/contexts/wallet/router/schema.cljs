(ns status-im.contexts.wallet.router.schema)

(def ?fee-modes
  [:enum
   :tx-fee-mode/custom
   :tx-fee-mode/normal
   :tx-fee-mode/fast
   :tx-fee-mode/urgent])

(def ^:private ?suggested-levels-for-max-fees-per-gas
  [:map
   [:low :schema.common/hex]
   [:medium :schema.common/hex]
   [:high :schema.common/hex]
   [:low-estimated-time nat-int?]
   [:medium-estimated-time nat-int?]
   [:high-estimated-time nat-int?]
   [:low-priority :schema.common/hex]
   [:medium-priority :schema.common/hex]
   [:high-priority :schema.common/hex]])

(def ^:private ?suggested-non-eip-1559-fees
  [:map
   [:gas-price :schema.common/hex]
   [:estimated-time nat-int?]])

(def ^:private ?chain
  [:map
   [:short-name :string]
   [:chain-id nat-int?]
   [:related-chain-id nat-int?]
   [:chain-name :string]
   [:native-currency-name :string]
   [:eip-1559-enabled :boolean]])

(def ?route
  [:map
   ;; Route/Tx info
   [:processor-name :string]
   [:router-input-params-uuid :string]
   [:tx-packed-data :string]
   [:tx-nonce :schema.common/hex]
   [:suggested-tx-nonce :schema.common/hex]
   [:used-contract-address :schema.common/hex]
   [:from-chain ?chain]
   [:to-chain ?chain]

   ;; Amounts
   [:amount-in :schema.common/hex]
   [:amount-out :schema.common/hex]
   [:required-native-balance nat-int?]
   [:required-token-balance nat-int?]

   ;; Fees
   [:tx-gas-amount nat-int?]
   [:tx-token-fees :schema.common/hex]
   [:tx-total-fee :schema.common/hex]
   [:tx-priority-fee [:maybe :schema.common/hex]]
   [:tx-base-fee [:maybe :schema.common/hex]]
   [:tx-gas-price [:maybe :schema.common/hex]]
   [:tx-fee [:maybe :schema.common/hex]]
   [:tx-l-1-fee [:maybe :schema.common/hex]]
   [:tx-bonder-fees [:maybe :schema.common/hex]]
   [:tx-gas-fee-mode nat-int?]
   [:tx-estimated-time nat-int?]
   [:tx-max-fees-per-gas [:maybe :schema.common/hex]]
   [:suggested-min-priority-fee [:maybe :schema.common/hex]]
   [:suggested-max-priority-fee [:maybe :schema.common/hex]]
   [:suggested-tx-gas-amount [:maybe nat-int?]]
   [:suggested-levels-for-max-fees-per-gas [:maybe ?suggested-levels-for-max-fees-per-gas]]
   [:suggested-non-eip-1559-fees [:maybe ?suggested-non-eip-1559-fees]]
   [:current-base-fee [:maybe :schema.common/hex]]
   [:subtract-fees :boolean]

   ;; Approval
   [:approval-required :boolean]
   [:approval-tx-nonce [:maybe :schema.common/hex]]
   [:suggested-approval-tx-nonce [:maybe :schema.common/hex]]
   [:suggested-approval-gas-amount nat-int?]
   [:approval-contract-address [:maybe :schema.common/hex]]
   [:approval-packed-data [:maybe :string]]
   [:approval-amount-required [:maybe :schema.common/hex]]
   [:approval-fee [:maybe :schema.common/hex]]
   [:approval-l-1-fee [:maybe :schema.common/hex]]
   [:approval-gas-amount [:maybe nat-int?]]
   [:approval-priority-fee [:maybe :schema.common/hex]]
   [:approval-base-fee [:maybe :schema.common/hex]]
   [:approval-gas-price [:maybe :schema.common/hex]]
   [:approval-max-fees-per-gas [:maybe :schema.common/hex]]
   [:approval-estimated-time [:maybe nat-int?]]
   [:approval-gas-fee-mode [:maybe nat-int?]]])
