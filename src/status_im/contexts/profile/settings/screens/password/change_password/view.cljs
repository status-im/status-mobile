(ns status-im.contexts.profile.settings.screens.password.change-password.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.profile.settings.screens.password.change-password.events]
    [status-im.contexts.profile.settings.screens.password.change-password.new-password-form :as
     new-password-form]
    [status-im.contexts.profile.settings.screens.password.change-password.old-password-form :as
     old-password-form]
    [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [{:keys [top bottom]}     (safe-area/get-insets)
        alert-banners-top-margin (rf/sub [:alert-banners/top-margin])
        current-step             (rf/sub [:settings/change-password-current-step])]
    (rn/use-unmount #(rf/dispatch [:change-password/reset]))
    [quo/overlay {:type :shell}
     [rn/pressable
      {:key        :change-password
       :on-press   rn/dismiss-keyboard!
       :accessible false
       :style      {:flex 1}}
      [quo/page-nav
       {:margin-top top
        :background :blur
        :icon-name  :i/arrow-left
        :on-press   navigate-back}]
      [rn/keyboard-avoiding-view
       {:style                    {:flex 1}
        :keyboard-vertical-offset (if platform/ios?
                                    (-> 12
                                        (+ alert-banners-top-margin)
                                        (- bottom))
                                    0)}
       (condp = current-step
         :old-password [old-password-form/view]
         :new-password [new-password-form/view])]]]))
