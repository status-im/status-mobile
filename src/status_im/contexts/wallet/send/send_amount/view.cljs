(ns status-im.contexts.wallet.send.send-amount.view
  (:require
    [quo.theme]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  [input-amount/view
   {:current-screen-id :screen/wallet.send-input-amount
    :button-one-label  (i18n/label :t/review-send)
    :on-navigate-back  (fn []
                         (rf/dispatch [:wallet/clean-disabled-from-networks])
<<<<<<< HEAD
<<<<<<< HEAD
                         (rf/dispatch [:wallet/clean-from-locked-amounts])
                         (rf/dispatch [:wallet/clean-send-amount])
                         (when-not hardware?
                           (rf/dispatch [:navigate-back])))}])
=======
                         (rf/dispatch [:wallet/clean-send-amount]))}])
>>>>>>> e5ac6dc2d (lint)
=======
                         (rf/dispatch [:wallet/clean-send-amount])
                         (rf/dispatch [:navigate-back]))}])
>>>>>>> 01ab1a81c (lint)
