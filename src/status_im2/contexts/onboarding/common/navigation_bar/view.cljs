(ns status-im2.contexts.onboarding.common.navigation-bar.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn navigation-bar
  [{:keys [top right-section-buttons disable-back-button? left-on-press root-comp-id]}]
  [rn/view
   {:style {:height     56
            :margin-top top}}
   [quo/page-nav
    {:align-mid?            true
     :mid-section           {:type :text-only :main-text ""}
     :left-section          {:type                :blur-bg
                             :icon                :i/arrow-left
                             :icon-override-theme :dark
                             :on-press            (when-not disable-back-button?
                                                    (fn []
                                                      (println root-comp-id "fdsfds")
                                                      (if root-comp-id
                                                        (rf/dispatch [:navigate-back-in-stack root-comp-id])
                                                        (rf/dispatch [:navigate-back]))
                                                      (when left-on-press
                                                        (left-on-press))))}
     :right-section-buttons right-section-buttons}]])
