(ns status-im.contexts.settings.wallet.keypairs-and-accounts.encrypted-qr.countdown.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]))

(def code-valid-for-ms 120000)
(def one-min-ms 60000)

(defn current-ms
  []
  (* 1000 (js/Math.ceil (/ (datetime/timestamp) 1000))))

(defn view
  [on-clear]
  (let [[valid-for-ms set-valid-for-ms] (rn/use-state code-valid-for-ms)
        [timestamp set-timestamp]       (rn/use-state current-ms)
        clock                           (rn/use-callback (fn []
                                                           (let [remaining (- code-valid-for-ms
                                                                              (- (current-ms)
                                                                                 timestamp))]
                                                             (when (pos? remaining)
                                                               (set-valid-for-ms remaining))
                                                             (when (zero? remaining)
                                                               (set-timestamp (current-ms))
                                                               (set-valid-for-ms code-valid-for-ms)
                                                               (on-clear))))
                                                         [code-valid-for-ms])]
    (hooks/use-interval clock on-clear 1000)
    [quo/text
     {:size  :paragraph-2
      :style {:color (if (< valid-for-ms one-min-ms)
                       colors/danger-60
                       colors/white-opa-40)}}
     (i18n/label :t/valid-for-time {:valid-for (datetime/ms-to-duration valid-for-ms)})]))
