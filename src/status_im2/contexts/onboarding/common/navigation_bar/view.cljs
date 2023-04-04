(ns status-im2.contexts.onboarding.common.navigation-bar.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn navigation-bar
  [{:keys [on-press-info]}]
  [rn/view {:style {:height 56}}
   [quo/page-nav
    {:align-mid?            true
     :mid-section           {:type :text-only}
     :left-section          {:type                :blur-bg
                             :icon                :i/arrow-left
                             :icon-override-theme :dark
                             :on-press            #(rf/dispatch [:navigate-back])}
     :right-section-buttons [{:type                :blur-bg
                              :icon                :i/info
                              :icon-override-theme :dark
                              :on-press            on-press-info}]}]])
