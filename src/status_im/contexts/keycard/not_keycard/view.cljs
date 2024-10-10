(ns status-im.contexts.keycard.not-keycard.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.events-helper :as events-helper]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [top bottom]} (safe-area/get-insets)]
    [quo/overlay
     {:type            :shell
      :container-style {:padding-top    top
                        :padding-bottom bottom}}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top
      {:title            (i18n/label :t/oops-not-keycard)
       :description      :text
       :description-text (i18n/label :t/make-sure-keycard)}]
     [rn/view {:flex 1}]
     [rn/view {:padding-horizontal 20}
      [quo/button {:on-press #(rf/dispatch [:keycard/connect])}
       (i18n/label :t/try-again)]]]))
