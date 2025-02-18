(ns status-im.contexts.shell.view
  (:require
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
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

(defn shell-stack
  []
  (let [shared-values (shared-values/calculate-and-set-shared-values)]
    (rn/use-mount
     (fn []
       (rn/hw-back-add-listener navigate-back-handler)
       #(rn/hw-back-remove-listener navigate-back-handler)))
    [rn/view {:style {:background-color colors/neutral-100 :flex 1}}
     [home-stack/view shared-values]
     [bottom-tabs/view shared-values]]))
