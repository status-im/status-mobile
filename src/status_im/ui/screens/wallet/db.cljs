(ns status-im.ui.screens.wallet.db)

;; Placeholder namespace for wallet specs, which are a WIP depending on data
;; model we decide on for balances, prices, etc.

;; TODO(oskarth): spec for balance as BigNumber
;; TODO(oskarth): Spec for prices as as: {:from ETH, :to USD, :price 290.11, :last-day 304.17}

(def dummy-transaction-data
  [{:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "0,4908" :symbol "ETH"} :state :unsigned}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :unsigned}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :unsigned}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "0,4908" :symbol "ETH"} :state :pending}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :pending}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :sent}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :postponed}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "0,4908" :symbol "ETH"} :state :pending}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :pending}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :sent}])
