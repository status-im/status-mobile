(ns status-im.contexts.keycard.not-keycard.view
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
    {:title            (i18n/label :t/oops-not-keycard)
     :description      :text
     :description-text (i18n/label :t/make-sure-keycard)}]
   [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
    [rn/image
     {:resize-mode :contain
      :source      (resources/get-image :not-keycard)}]]
   [rn/view {:padding-horizontal 20}
    [quo/button {:on-press events-helper/navigate-back}
     (i18n/label :t/try-again)]]])
