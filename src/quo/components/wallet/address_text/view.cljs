(ns quo.components.wallet.address-text.view
  (:require [clojure.string :as string]
            [quo.components.markdown.text :as text]
            [quo.components.wallet.address-text.schema :as component-schema]
            [quo.components.wallet.address-text.style :as style]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [schema.core :as schema]
            [utils.address :as utils]))

(defn- colored-network-text
  [{:keys [theme network size weight]}]
  [text/text
   {:size   size
    :weight weight
    :style  {:color (colors/resolve-color (keyword network) theme)}}
   (str network ":")])

(defn- view-internal
  [{:keys [networks address blur? theme format full-address? size weight]
    :or   {size :paragraph-2}}]
  (let [network-colored-text                      (map #(colored-network-text {:theme   theme
                                                                          :network %
                                                                          :weight  weight
                                                                          :size    size}))
        [splitted-networks splitted-address] (and full-address?
                                                  (as-> address $
                                                    (string/split $ ":")
                                                    [(butlast $) (last $)]))
        address-internal                     (if full-address? splitted-address address)
        networks-internal                    (if full-address?
                                               splitted-networks
                                               (map :short-name networks))
        address-text                         [text/text
                                              {:size   size
                                               ;; TODO: monospace font
                                               ;; https://github.com/status-im/status-mobile/issues/17009
                                               :weight (or weight :monospace)
                                               :style  (style/address-text format blur? theme)}
                                              (if (= format :short)
                                                (utils/get-short-wallet-address address-internal)
                                                address-internal)]]
    (as-> networks-internal $
      (into [text/text] network-colored-text $)
      (conj $ address-text))))

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal component-schema/?schema)))
