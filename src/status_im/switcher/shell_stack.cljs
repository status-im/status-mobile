(ns status-im.switcher.shell-stack
  (:require [status-im.i18n.i18n :as i18n]
            [status-im.switcher.shell :as shell]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.animation :as animation]
            [status-im.switcher.home-stack :as home-stack]
            [status-im.switcher.bottom-tabs :as bottom-tabs]
            [quo2.components.navigation.floating-shell-button :as floating-shell-button]))

(defn shell-stack []
  [:f>
   (fn []
     (let [shared-values (animation/get-shared-values)]
       [:<>
        [shell/shell]
        [bottom-tabs/bottom-tabs shared-values]
        [home-stack/home-stack shared-values]
        [floating-shell-button/floating-shell-button
         {:jump-to {:on-press #(animation/close-home-stack shared-values)
                    :label (i18n/label :t/jump-to)}}
         {:position :absolute
          :bottom   (+ (constants/bottom-tabs-container-height) 7)} ;; bottom offset is 12 = 7 + 5(padding on button)
         (:home-stack-opacity shared-values)
         (:home-stack-pointer shared-values)]]))])
