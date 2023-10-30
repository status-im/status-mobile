(ns quo.components.wallet.address-text.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.wallet.address-text.style :as style]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [utils.address :as utils]))

(defn- colored-network-text
  [theme network]
  (let [{:keys [name short]} network]
  [text/text
   {:size  :paragraph-2
    :style {:color (colors/resolve-color name theme)}}
   (str short ":")]))

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
    (as-> networks $ ;; Take vector of networks
      (into [text/text] network-text-xf $) ;; make colored text and inserts them into [text/text]
      (conj $ address-text)))) ;; Add address-text hiccup at the end

(def view (quo.theme/with-theme view-internal))
