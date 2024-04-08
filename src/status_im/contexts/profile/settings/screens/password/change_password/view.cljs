(ns status-im.contexts.profile.settings.screens.password.change-password.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.profile.settings.screens.password.change-password.events]
    [status-im.contexts.profile.settings.screens.password.change-password.new-password-form :as
     new-password-form]
    [status-im.contexts.profile.settings.screens.password.change-password.old-password-form :as
     old-password-form]
    [status-im.contexts.profile.settings.screens.password.change-password.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [{:keys [keyboard-shown]} (hooks/use-keyboard)
        {:keys [top bottom]}     (safe-area/get-insets)
        current-step             (or (rf/sub
                                      [:settings/change-password-current-step])
                                     :old-password)]
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
      [rn/keyboard-avoiding-view {:style style/form-container}
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
         (i18n/label :t/change-password-description)]]
       (condp = current-step
         :old-password [old-password-form/view]
         :new-password [new-password-form/view])
       [rn/view {:style {:height (if-not keyboard-shown bottom 0)}}]]]]))
