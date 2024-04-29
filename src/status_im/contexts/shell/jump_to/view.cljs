(ns status-im.contexts.shell.jump-to.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.shell.jump-to.animation :as animation]
    [status-im.contexts.shell.jump-to.components.bottom-tabs.view :as bottom-tabs]
    [status-im.contexts.shell.jump-to.components.floating-screens.view :as floating-screens]
    [status-im.contexts.shell.jump-to.components.home-stack.view :as home-stack]
    [status-im.contexts.shell.jump-to.components.jump-to-screen.view :as jump-to-screen]
    [status-im.contexts.shell.jump-to.shared-values :as shared-values]
    [status-im.contexts.shell.jump-to.utils :as utils]
    [status-im.navigation.state :as navigation.state]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn navigate-back-handler
  []
  (when (or (seq @navigation.state/modals)
            (seq (utils/open-floating-screens)))
    (rf/dispatch [:navigate-back])
    true))

(defn floating-button
  [shared-values]
  (let [current-screen-id (rf/sub [:view-id])]
    (when-not (= current-screen-id :settings)
      [quo/floating-shell-button
       {:jump-to {:on-press            #(animation/close-home-stack true)
                  :label               (i18n/label :t/jump-to)
                  :customization-color (rf/sub [:profile/customization-color])}}
       {:position :absolute
        :bottom   (utils/bottom-tabs-container-height)}
       (:home-stack-opacity shared-values)])))

(defn f-shell-stack
  []
  (let [alert-banners-top-margin (rf/sub [:alert-banners/top-margin])
        shared-values            (shared-values/calculate-and-set-shared-values
                                  alert-banners-top-margin)]
    (rn/use-mount
     (fn []
       (rn/hw-back-add-listener navigate-back-handler)
       #(rn/hw-back-remove-listener navigate-back-handler)))
    [:<>
     [jump-to-screen/view]
     [:f> bottom-tabs/f-bottom-tabs]
     [:f> home-stack/f-home-stack]
     [floating-button shared-values]
     [floating-screens/view]]))

(defn shell-stack
  []
  [:f> f-shell-stack])
