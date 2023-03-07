(ns status-im2.contexts.onboarding.common.page-nav
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [utils.re-frame :as rf]
    [react-native.core :as rn]))

(defn navigate-back
  []
  [rn/view {:margin-top 44}
   [quo/page-nav
    (merge {:horizontal-description? false
            :one-icon-align-left?    true
            :align-mid?              false
            :page-nav-color          :transparent
            :mid-section             {:type            :text-with-description
                                      :main-text       nil
                                      :description-img nil}
            :left-section            {:icon                  :i/arrow-left
                                      :icon-background-color colors/neutral-40
                                      :on-press              #(rf/dispatch [:navigate-back])}})]])