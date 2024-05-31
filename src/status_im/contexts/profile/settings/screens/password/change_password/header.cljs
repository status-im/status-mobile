(ns status-im.contexts.profile.settings.screens.password.change-password.header
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.profile.settings.screens.password.change-password.style :as style]
            [utils.i18n :as i18n]))

(defn view
  []
  [rn/view {:style style/heading}
   [quo/text
    {:style  style/heading-title
     :weight :semi-bold
     :size   :heading-1}
    (i18n/label :t/change-password)]
   [quo/text
    {:style  style/heading-subtitle
     :weight :regular
     :size   :paragraph-1}
    (i18n/label :t/change-password-description)]])
