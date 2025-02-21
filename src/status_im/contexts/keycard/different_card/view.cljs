(ns status-im.contexts.keycard.different-card.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [utils.i18n :as i18n]))

(defn view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/different-keycard)
     :description      :text
     :description-text (i18n/label :t/scan-previous-keycard)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :keycard-not-same)}]
   [rn/view {:style {:padding-horizontal 20 :padding-vertical 12}}
    [quo/button {:on-press events-helper/navigate-back}
     (i18n/label :t/try-again)]]])

