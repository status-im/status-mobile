(ns quo.components.wallet.address-text.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.wallet.address-text.style :as style]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.pure :as rn.pure]
            [utils.address :as utils]))

(defn- colored-network-text
  [network theme]
  (let [{:keys [network-name short-name]} network]
    (text/text
     {:size  :paragraph-2
      :style {:color (colors/resolve-color network-name theme)}}
     (str short-name ":"))))

(defn- view-pure
  [{:keys [networks address blur? format]}]
  (let [theme        (quo.theme/use-theme)
        address-text (text/text
                      {:size   :paragraph-2
                       ;; TODO: monospace font https://github.com/status-im/status-mobile/issues/17009
                       :weight :monospace
                       :style  (style/address-text format blur? theme)}
                      (if (= format :short)
                        (utils/get-short-wallet-address address)
                        address))]
    (apply text/text
           (-> (mapv #(colored-network-text % theme) networks)
               (conj address-text)))))

(defn view
  [params]
  (rn.pure/func view-pure params))
