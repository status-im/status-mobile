(ns status-im.contexts.wallet.send.transaction-confirmation.transaction-title
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.utils :as rn.utils]
            [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- title-container
  [& argv]
  (let [[_ children] (rn.utils/get-props-and-children argv)]
    (into [rn/view {:style style/content-container}]
          children)))

(defn- title-row
  [& argv]
  (let [[_ children] (rn.utils/get-props-and-children argv)]
    (into [rn/view
           {:style {:flex-direction :row
                    :margin-top     4}}]
          children)))

(defn- title-text
  [text]
  [quo/text
   {:size                :heading-1
    :weight              :semi-bold
    :style               style/title-container
    :accessibility-label :send-label}
   text])

(defn- title-token
  []
  (let [amount             (rf/sub [:wallet/send-amount])
        token-display-name (rf/sub [:wallet/wallet-send-token-symbol])]
    [quo/summary-tag
     {:token token-display-name
      :label (str amount " " token-display-name)
      :type  :token}]))

(defn- title-collectible
  []
  (let [{:keys [collectible-data preview-url]} (rf/sub [:wallet/send-collectible])]
    [quo/summary-tag
     {:label        (:name collectible-data)
      :image-source (:uri preview-url)
      :type         :collectible}]))

(defn- title-network
  [{:keys [chain-id]}]
  (let [{:keys [full-name source network-name]} (rf/sub [:wallet/network-details-by-chain-id chain-id])]
    [quo/summary-tag
     {:label               full-name
      :type                :network
      :image-source        source
      :customization-color network-name}]))

(defn- from-bridge-networks
  []
  (let [chain-ids (rf/sub [:wallet/send-from-chain-ids])]
    [:<>
     (doall (map-indexed (fn [idx chain-id]
                           ^{:key (str "transaction-title" idx)}
                           [title-row
                            [title-text
                             (if (zero? idx)
                               (i18n/label :t/from)
                               (str (i18n/label :t/and) " "))]
                            [title-network {:chain-id chain-id}]])
                         chain-ids))]))

(defn- to-bridge-network
  []
  (let [chain-id (rf/sub [:wallet/send-bridge-to-chain-id])]
    [title-network {:chain-id chain-id}]))

(defn- title-account
  []
  (let [{:keys [name emoji color]} (rf/sub [:wallet/current-viewing-account])]
    [quo/summary-tag
     {:label               name
      :type                :account
      :emoji               emoji
      :customization-color color}]))

(defn- title-recipient
  []
  (let [{:keys [recipient-type emoji label customization-color]} (rf/sub [:wallet/send-recipient])]
    [quo/summary-tag
     {:type                recipient-type
      :emoji               emoji
      :label               label
      :customization-color customization-color}]))

(defn- bridge-title
  []
  [title-container
   [title-row
    [title-text (i18n/label :t/bridge)]
    [title-token]]
   [from-bridge-networks]
   [title-row
    [title-text (i18n/label :t/to)]
    [to-bridge-network]]
   [title-row
    [title-text (i18n/label :t/in)]
    [title-account]]])

(defn- send-title
  []
  (let [sending-collectible? (rf/sub [:wallet/sending-collectible?])]
    [title-container
     [title-row
      [title-text (i18n/label :t/send)]
      (if sending-collectible?
        [title-collectible]
        [title-token])]
     [title-row
      [title-text (i18n/label :t/from)]
      [title-account]]
     [title-row
      [title-text (i18n/label :t/to)]
      [title-recipient]]]))

(defn view
  []
  (let [tx-type (rf/sub [:wallet/send-tx-type])]
    (condp = tx-type
      :tx/bridge [bridge-title]
      :tx/send   [send-title]
      [send-title])))
