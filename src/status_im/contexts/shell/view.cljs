(ns status-im.contexts.shell.view
  (:require
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.config :as config]
    [status-im.contexts.shell.bottom-tabs.view :as bottom-tabs]
    [status-im.contexts.shell.home-stack.view :as home-stack]
    [status-im.contexts.shell.shared-values :as shared-values]
    [status-im.navigation.state :as navigation.state]
    [utils.re-frame :as rf]))

(defn- navigate-back-handler
  []
  (when (or (seq @navigation.state/modals)
            (> (count (navigation.state/get-navigation-state)) 1))
    (rf/dispatch [:navigate-back])
    true))

;; A hidden view-id-tracker view, required for e2e testing
(defn- view-id-tracker
  []
  (let [view-id (rf/sub [:view-id])]
    [rn/view
     {:accessible          true
      :accessibility-label :view-id-tracker
      :style               {:width 1 :height 1 :margin-top -1}}
     [rn/text {:color :transparent} view-id]]))

(defn shell-stack
  []
  (let [privacy-mode-enabled? (rf/sub [:privacy-mode/privacy-mode-enabled?])
        unselected-tab-color  (if privacy-mode-enabled? colors/white-opa-40 colors/neutral-50)
        shared-values         (shared-values/calculate-and-set-shared-values unselected-tab-color)
        background-color      (if privacy-mode-enabled?
                                (colors/resolve-color :purple :dark)
                                colors/neutral-100)]
    (rn/use-mount
     (fn []
       (rn/hw-back-add-listener navigate-back-handler)
       #(rn/hw-back-remove-listener navigate-back-handler)))
    [rn/view {:style {:background-color background-color :flex 1}}
     [home-stack/view shared-values]
     (when config/enable-view-id-tracker?
       [view-id-tracker])
     [bottom-tabs/view shared-values privacy-mode-enabled?]]))
