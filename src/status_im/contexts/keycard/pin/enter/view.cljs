(ns status-im.contexts.keycard.pin.enter.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.contexts.keycard.pin.view :as keycard.pin]
            [utils.i18n :as i18n]))

(defn view
  []
  (let [{:keys [on-complete title]} (quo.theme/use-screen-params)]
    [rn/view {:style {:padding-bottom 12 :flex 1}}
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top {:title (or title (i18n/label :t/enter-keycard-pin))}]
     [quo/keycard]
     [keycard.pin/auth {:on-complete on-complete}]]))
