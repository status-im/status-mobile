(ns status-im.contexts.wallet.common.fiat-text.view
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.common.utils :as utils]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [amount] :as text-props}]
  (let [currency-symbol (rf/sub [:profile/currency-symbol])
        pretty-amount   (utils/prettify-balance currency-symbol amount)]
    [quo/text text-props pretty-amount]))
