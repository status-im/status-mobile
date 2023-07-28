(ns status-im2.contexts.onboarding.common.navigation-bar.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn navigation-bar
  [{:keys [top right-section-buttons disable-back-button? stack-id]}]
  (let [back-event (if stack-id [:navigate-back-within-stack stack-id] [:navigate-back])]
    [rn/view
     {:style {:height     56
              :margin-top top}}
     [quo/page-nav
      {:align-mid?            true
       :mid-section           {:type :text-only :main-text ""}
       :left-section          {:type                :grey
                               :icon-background :blur
                               :icon                :i/arrow-left
                               :on-press            (fn []
                                                      (when-not disable-back-button?
                                                        (rf/dispatch back-event)))}
       :right-section-buttons right-section-buttons}]]))
