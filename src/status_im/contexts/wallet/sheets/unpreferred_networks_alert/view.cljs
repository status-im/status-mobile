(ns status-im.contexts.wallet.sheets.unpreferred-networks-alert.view
  (:require [quo.core :as quo]
            [re-frame.core :as rf]
            [status-im.contexts.wallet.sheets.unpreferred-networks-alert.style :as style]
            [utils.i18n :as i18n]))

(defn view
  [{:keys [on-confirm]}]
  [:<>
   [quo/text
    {:style  style/sending-to-unpreferred-networks-title
     :size   :heading-2
     :weight :semi-bold}
    (i18n/label :t/sending-to-unpreferred-networks)]
   [quo/text
    {:style style/sending-to-unpreferred-networks-text
     :size  :paragraph-1}
    (i18n/label :t/sending-to-networks-the-receiver-does-not-prefer)]
   [quo/bottom-actions
    {:actions          :two-actions
     :button-two-label (i18n/label :t/cancel)
     :button-two-props {:on-press #(rf/dispatch [:hide-bottom-sheet])
                        :type     :grey}
     :button-one-label (i18n/label :t/proceed-anyway)
     :button-one-props {:on-press            (fn []
                                               (rf/dispatch [:hide-bottom-sheet])
                                               (when on-confirm (on-confirm)))
                        :customization-color :danger}}]])
