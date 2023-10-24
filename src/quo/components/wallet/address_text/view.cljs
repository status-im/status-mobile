(ns quo.components.wallet.address-text.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.wallet.address-text.style :as style]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [utils.address :as utils]))

(defn- colored-network-text
  [theme network]
  (let [{:keys [name short-name]} network]
    [text/text
     {:size  :paragraph-2
      :style {:color (colors/resolve-color name theme)}}
     (str short-name ":")]))

(defn- view-internal
  [{:keys [networks address blur? theme format]}]
  (let [network-text-xf (map #(colored-network-text theme %))
        address-text    [text/text
                         {:size   :paragraph-2
                          ;; TODO: monospace font https://github.com/status-im/status-mobile/issues/17009
                          :weight :monospace
                          :style  (style/address-text format blur? theme)}
                         (if (= format :short)
                           (utils/get-short-wallet-address address)
                           address)]]
    (as-> networks $
      (into [text/text] network-text-xf $)
      (conj $ address-text))))

(def view (quo.theme/with-theme view-internal))
