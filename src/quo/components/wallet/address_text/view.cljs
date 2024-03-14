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
  [theme network]
  (let [{:keys [network-name short-name]} network]
    [text/text
     {:size  :paragraph-2
      :style {:color (colors/resolve-color network-name theme)}}
     (str short-name ":")]))

(defn- view-internal
  [{:keys [networks address blur? theme format full-address?]}]
  (let [network-text-xf (map #(colored-network-text theme %))
        [splitted-networks splitted-address] (and full-address? (as-> address $
                                                                  (string/split $ ":")
                                                                  [(butlast $) (last $)]))
        address-interval (if full-address? splitted-address address)
        address-text    [text/text
                         {:size   :paragraph-2
                          ;; TODO: monospace font
                          ;; https://github.com/status-im/status-mobile/issues/17009
                          :weight :monospace
                          :style  (style/address-text format blur? theme)}
                         (if (= format :short)
                           (utils/get-short-wallet-address address-interval)
                           address-interval)]]
    (as-> (if full-address? splitted-networks networks) $
      (into [text/text] network-text-xf $)
      (conj $ address-text))))

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal component-schema/?schema)))
